# Guali Dev — Proyecto Desarrollo Web (Fase 2)

Aplicación web para practicar programación en Java con lecciones cortas: el estudiante escribe el
código **a mano**, lo compila y lo ejecuta, y el sistema evalúa automáticamente el objetivo de la lección.

## Arquitectura (microservicios)

```
Internet ─▶ CloudFront (HTTPS · CDN · Shield anti-DDoS · WAF) ─▶ Nginx (reverse proxy + límites)
                                                     │
                                                     ▼
                                            guali-web  (Spring Boot 4, MVC + REST + Security + JPA)
                                              │                         │
                                     REST interno (token)               │ JDBC
                                              ▼                         ▼
                                      guali-compiler                 MariaDB
                                  (Spring Boot 4 + guali-dev.jar)
```

| Módulo | Puerto | Responsabilidad |
|---|---|---|
| `guali-web` | 8080 | Páginas (Thymeleaf + HTML/CSS/JS), API REST, lecciones, historial, configuración, administración, seguridad |
| `guali-compiler` | 8081 | Compila y ejecuta el código con `guali-dev-1.0.0.jar`. Sin base de datos y sin acceso desde internet |
| `mariadb` | 3306 | Base de datos relacional |
| `nginx` | 80 | Reverse proxy con limitación de peticiones y encabezados de seguridad |

## Requisitos

- **JDK 25** (obligatorio: `guali-dev-1.0.0.jar` está compilado con Java 25)
- Maven 3.9+ (IntelliJ ya lo trae)
- MariaDB 10.6+ / 11
- (Opcional) Docker Desktop para levantar todo con un solo comando

## Ejecutar en tu computadora (IntelliJ)

1. **Base de datos** — enciende MySQL (MariaDB) en XAMPP. Por defecto la aplicación usa
   `localhost:3307`, usuario `root` sin contraseña, y crea sola la base `guali_db` y sus tablas.
   Si tu MariaDB es distinta, define las variables `GUALI_DB_URL`, `GUALI_DB_USER` y `GUALI_DB_PASSWORD`.

2. **Abrir el proyecto** — en IntelliJ: *File → Open* y selecciona el `pom.xml` de la carpeta raíz.
   Verifica en *File → Project Structure → Project* que el SDK sea **25**.

3. **Arrancar el compilador** — ejecuta `GualiCompilerApplication` (módulo `guali-compiler`).
   Usa automáticamente el JDK con el que corre. Si quieres otro, define la variable
   `GUALI_JDK_HOME` (ej. `C:/Program Files/Java/jdk-25`).

4. **Arrancar la web** — ejecuta `GualiWebApplication` (módulo `guali-web`).

5. Abre **http://localhost:8080**

   - Estudiante: entra directo, sin usuario.
   - Administrador: *Acceso administrador* → usuario `admin`, contraseña `Admin123!`
     (cámbiala en *Administración → Usuarios*).
   - La primera vez se cargan 5 lecciones de ejemplo automáticamente.

## Ejecutar con Docker (todo junto)

```bash
cp .env.example .env      # y cambia las contraseñas
docker compose up -d --build
```
Abre **http://localhost**. Para ver los logs: `docker compose logs -f guali-web`.

## Funcionalidades

| Requisito | Dónde está |
|---|---|
| Interfaz: menú, título de lección, panel de lección, editor, consola | `templates/index.html`, `static/js/app.js`, `static/css/app.css` |
| Abrir / Cerrar lección | Menú *Lección* → `POST /api/sesiones`, `POST /api/sesiones/{id}/cerrar` |
| Compilar / Ejecutar (Ejecutar solo si la última compilación fue exitosa) | `POST /api/codigo/compilar`, `POST /api/codigo/ejecutar` → microservicio `guali-compiler` |
| Evaluación de objetivos (4 tipos de `config.txt`) | `EvaluadorObjetivo.java` |
| Historial persistente (Lección, Fecha inicio, Fecha final, Estado) | Menú *Historial* → `GET /api/historial`, tabla `historial` |
| Configuración: tamaño de fuente y tema claro/oscuro (guardado en BD) | Menú *Configuración* → `GET/PUT /api/preferencias`, tabla `preferencia` |
| Importar compendio .zip (admin) | `POST /api/admin/lecciones/importar` |
| Exportar lecciones a .zip (admin) | `GET /api/admin/lecciones/exportar` |
| Eliminar lección (admin) | `DELETE /api/admin/lecciones/{id}` |
| CRUD de administradores | `/admin/usuarios` |
| Estadísticas de visitas por intervalo | `/admin/estadisticas` |
| Ayuda | Menú *Ayuda* (o F1) |

## Formato de un compendio de lecciones (.zip)

```
lecciones/
  leccion-01-hola-mundo/
    leccion.md      ← texto de la lección (markdown)
    config.txt      ← objetivo=EJECUTAR_EXITOSO
    ciclo.png       ← imágenes usadas en el markdown (opcional)
```
Objetivos válidos: `COMPILAR_CON_ERROR`, `COMPILAR_EXITOSO`, `EJECUTAR_CON_ERROR`, `EJECUTAR_EXITOSO`.
Hay un ejemplo listo en `lecciones-ejemplo/lecciones-ejemplo.zip`.

## Estructura del código

```
guali/
├── pom.xml                      (agrupa los dos módulos)
├── docker-compose.yml, .env.example, nginx/nginx.conf
├── lecciones-ejemplo/           (bundle de prueba)
├── guali-compiler/
│   ├── libs/…/guali-dev-1.0.0.jar   (repositorio Maven local con la librería del catedrático)
│   └── src/main/java/…/compiler/    Controller · Service · filtro de token
└── guali-web/
    └── src/main/java/…/web/
        ├── modelo/        Entidades JPA (Leccion, RecursoLeccion, RegistroHistorial, Usuario, Visita, Preferencia)
        ├── repositorio/   Spring Data JPA
        ├── servicio/      Lógica de negocio (lecciones, zip, historial, evaluación, usuarios, visitas)
        ├── web/           Controladores MVC (páginas) y web/api (REST)
        ├── dto/           Objetos de transferencia
        └── config/        Seguridad, cookie anónima del estudiante, datos iniciales
```
