package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.modelo.Objetivo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Lee y escribe bundles de lecciones en formato .zip.
 *
 * Estructura esperada (la carpeta raíz "lecciones/" es opcional):
 * <pre>
 * lecciones/
 *   leccion-01-hola-mundo/
 *     leccion.md
 *     config.txt        (objetivo=COMPILAR_EXITOSO)
 *     imagen.jpg        (opcional)
 * </pre>
 * Es una clase sin dependencias de Spring para poder probarla fácilmente.
 */
public final class LeccionZip {

    public static final String ARCHIVO_MD = "leccion.md";
    public static final String ARCHIVO_CONFIG = "config.txt";

    private static final int MAX_ENTRADAS = 2000;
    private static final long MAX_BYTES_TOTAL = 60L * 1024 * 1024;   // protección "zip bomb"
    private static final Pattern PATRON_OBJETIVO =
            Pattern.compile("^\\s*objetivo\\s*=\\s*([A-Za-z_]+)\\s*$", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> TIPOS = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp",
            "svg", "image/svg+xml",
            "bmp", "image/bmp");

    public record Archivo(String nombre, String tipoContenido, byte[] datos) {
    }

    public record LeccionArchivo(String titulo, Objetivo objetivo, String contenidoMd, List<Archivo> recursos) {
    }

    public record Resultado(List<LeccionArchivo> lecciones, List<String> advertencias) {
    }

    private LeccionZip() {
    }

    // ------------------------------------------------------------------ lectura

    public static Resultado leer(byte[] zip) throws IOException {
        try {
            return leer(zip, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Zips creados en Windows a veces usan otra codificación para los nombres
            return leer(zip, Charset.forName("IBM437"));
        }
    }

    private static Resultado leer(byte[] zip, Charset charset) throws IOException {
        // ruta completa del archivo -> contenido
        Map<String, byte[]> archivos = new LinkedHashMap<>();
        long total = 0;
        int entradas = 0;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip), charset)) {
            ZipEntry entrada;
            while ((entrada = zis.getNextEntry()) != null) {
                if (++entradas > MAX_ENTRADAS) {
                    throw new IllegalStateException("El zip tiene demasiados archivos (máximo " + MAX_ENTRADAS + ").");
                }
                String ruta = normalizar(entrada.getName());
                if (entrada.isDirectory() || ruta.isEmpty() || debeIgnorarse(ruta)) {
                    continue;
                }
                byte[] datos = leerLimitado(zis, MAX_BYTES_TOTAL - total);
                total += datos.length;
                archivos.put(ruta, datos);
            }
        }

        if (archivos.isEmpty()) {
            throw new IllegalStateException("El archivo no es un zip válido o está vacío.");
        }

        // Carpetas de lección = carpetas que contienen leccion.md
        List<String> carpetas = new ArrayList<>();
        for (String ruta : archivos.keySet()) {
            if (nombreArchivo(ruta).equalsIgnoreCase(ARCHIVO_MD)) {
                carpetas.add(carpeta(ruta));
            }
        }
        // Las carpetas más profundas primero, para asignar cada archivo a la lección más específica
        carpetas.sort(Comparator.comparingInt(String::length).reversed());

        List<String> advertencias = new ArrayList<>();
        Map<String, List<String>> archivosPorLeccion = new LinkedHashMap<>();
        carpetas.forEach(c -> archivosPorLeccion.put(c, new ArrayList<>()));
        for (String ruta : archivos.keySet()) {
            for (String c : carpetas) {
                if (c.isEmpty() || ruta.startsWith(c + "/")) {
                    archivosPorLeccion.get(c).add(ruta);
                    break;
                }
            }
        }

        List<LeccionArchivo> lecciones = new ArrayList<>();
        for (String c : carpetas) {
            String titulo = c.isEmpty() ? "leccion" : nombreArchivo(c);
            String prefijo = c.isEmpty() ? "" : c + "/";
            String md = null;
            String config = null;
            List<Archivo> recursos = new ArrayList<>();

            for (String ruta : archivosPorLeccion.get(c)) {
                String relativa = ruta.substring(prefijo.length());
                byte[] datos = archivos.get(ruta);
                if (relativa.equalsIgnoreCase(ARCHIVO_MD)) {
                    md = texto(datos);
                } else if (relativa.equalsIgnoreCase(ARCHIVO_CONFIG)) {
                    config = texto(datos);
                } else {
                    String tipo = tipoContenido(relativa);
                    if (tipo != null) {
                        recursos.add(new Archivo(relativa, tipo, datos));
                    } else {
                        advertencias.add(titulo + ": se ignoró '" + relativa + "' (solo se aceptan imágenes).");
                    }
                }
            }

            if (config == null) {
                advertencias.add(titulo + ": no tiene config.txt, no se importó.");
                continue;
            }
            Objetivo objetivo = leerObjetivo(config);
            if (objetivo == null) {
                advertencias.add(titulo + ": config.txt no tiene un objetivo válido, no se importó.");
                continue;
            }
            if (titulo.length() > 200) {
                advertencias.add(titulo.substring(0, 40) + "...: el nombre de la carpeta es demasiado largo.");
                continue;
            }
            lecciones.add(new LeccionArchivo(titulo, objetivo, md, recursos));
        }

        lecciones.sort(Comparator.comparing(LeccionArchivo::titulo));
        if (lecciones.isEmpty() && advertencias.isEmpty()) {
            advertencias.add("No se encontró ninguna carpeta con un archivo leccion.md.");
        }
        return new Resultado(lecciones, advertencias);
    }

    /** Lee "objetivo=XXXX" de config.txt. Devuelve null si no es válido. */
    public static Objetivo leerObjetivo(String config) {
        if (config == null) {
            return null;
        }
        for (String linea : config.split("\\R")) {
            Matcher m = PATRON_OBJETIVO.matcher(linea.replace("﻿", ""));
            if (m.matches()) {
                try {
                    return Objetivo.valueOf(m.group(1).toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /** Tipo MIME según la extensión; null si no es una imagen permitida. */
    public static String tipoContenido(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) {
            return null;
        }
        return TIPOS.get(nombre.substring(punto + 1).toLowerCase(Locale.ROOT));
    }

    // ---------------------------------------------------------------- escritura

    /** Genera un zip con la misma estructura que se importa. */
    public static byte[] escribir(List<LeccionArchivo> lecciones) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(salida, StandardCharsets.UTF_8)) {
            for (LeccionArchivo l : lecciones) {
                String base = "lecciones/" + l.titulo() + "/";
                agregar(zos, base + ARCHIVO_MD, l.contenidoMd().getBytes(StandardCharsets.UTF_8));
                agregar(zos, base + ARCHIVO_CONFIG,
                        ("objetivo=" + l.objetivo().name() + "\n").getBytes(StandardCharsets.UTF_8));
                for (Archivo r : l.recursos()) {
                    agregar(zos, base + r.nombre(), r.datos());
                }
            }
        }
        return salida.toByteArray();
    }

    private static void agregar(ZipOutputStream zos, String nombre, byte[] datos) throws IOException {
        zos.putNextEntry(new ZipEntry(nombre));
        zos.write(datos);
        zos.closeEntry();
    }

    // ---------------------------------------------------------------- utilidades

    private static String normalizar(String nombre) {
        String r = nombre.replace('\\', '/');
        while (r.startsWith("/")) {
            r = r.substring(1);
        }
        if (r.endsWith("/")) {
            r = r.substring(0, r.length() - 1);
        }
        return r;
    }

    /** Ignora basura de macOS/Windows y rutas sospechosas. */
    private static boolean debeIgnorarse(String ruta) {
        for (String parte : ruta.split("/")) {
            if (parte.startsWith(".") || parte.equals("__MACOSX") || parte.equalsIgnoreCase("Thumbs.db")
                    || parte.equals("..")) {
                return true;
            }
        }
        return false;
    }

    private static String carpeta(String ruta) {
        int i = ruta.lastIndexOf('/');
        return i < 0 ? "" : ruta.substring(0, i);
    }

    private static String nombreArchivo(String ruta) {
        int i = ruta.lastIndexOf('/');
        return i < 0 ? ruta : ruta.substring(i + 1);
    }

    private static String texto(byte[] datos) {
        String s = new String(datos, StandardCharsets.UTF_8);
        return s.startsWith("﻿") ? s.substring(1) : s;
    }

    private static byte[] leerLimitado(InputStream in, long disponible) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long leidos = 0;
        int n;
        while ((n = in.read(buffer)) != -1) {
            leidos += n;
            if (leidos > disponible) {
                throw new IllegalStateException("El contenido del zip es demasiado grande (máximo 60 MB descomprimido).");
            }
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
