/*
 * Renderizador de Markdown pequeño y seguro (sin dependencias, funciona offline).
 * Primero escapa todo el HTML del texto y luego aplica el formato, por lo que
 * un archivo leccion.md no puede inyectar <script> ni HTML propio (evita XSS).
 *
 * Soporta: títulos, párrafos, negrita, cursiva, tachado, código en línea,
 * bloques de código ```, citas >, listas (- * + y 1.), tablas, imágenes,
 * enlaces y líneas horizontales.
 */
(function (global) {
    'use strict';

    function escapar(s) {
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function desescapar(s) {
        return String(s)
            .replace(/&#39;/g, "'")
            .replace(/&quot;/g, '"')
            .replace(/&gt;/g, '>')
            .replace(/&lt;/g, '<')
            .replace(/&amp;/g, '&');
    }

    /** Solo permite http(s), mailto, rutas relativas y anclas. */
    function urlSegura(url) {
        const u = url.trim();
        if (/^[a-z][a-z0-9+.-]*:/i.test(u) && !/^(https?:|mailto:)/i.test(u)) {
            return '#';
        }
        return u;
    }

    function enLinea(texto, opciones) {
        const reservados = [];
        const guardar = (html) => {
            reservados.push(html);
            return '\u0000' + (reservados.length - 1) + '\u0000';
        };

        // 1) código en línea (su contenido no se formatea)
        let s = texto.replace(/`([^`]+)`/g, (m, c) => guardar('<code>' + escapar(c) + '</code>'));

        // 2) escapar todo lo demás
        s = escapar(s);

        // 3) imágenes ![alt](src)
        s = s.replace(/!\[([^\]]*)\]\(\s*([^)\s]+)(?:\s+&quot;[^)]*&quot;)?\s*\)/g, (m, alt, src) => {
            const destino = opciones.resolverImagen(urlSegura(desescapar(src)));
            return guardar('<img alt="' + alt + '" src="' + escapar(destino) + '" loading="lazy">');
        });

        // 4) enlaces [texto](url)
        s = s.replace(/\[([^\]]+)\]\(\s*([^)\s]+)(?:\s+&quot;[^)]*&quot;)?\s*\)/g, (m, t, url) => {
            const destino = urlSegura(desescapar(url));
            return guardar('<a href="' + escapar(destino) + '" target="_blank" rel="noopener noreferrer">' + t + '</a>');
        });

        // 5) énfasis
        s = s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
             .replace(/__(.+?)__/g, '<strong>$1</strong>')
             .replace(/~~(.+?)~~/g, '<del>$1</del>')
             .replace(/(^|[^*])\*(?!\s)(.+?)\*(?!\*)/g, '$1<em>$2</em>')
             .replace(/(^|[^\w])_(?!\s)(.+?)_(?!\w)/g, '$1<em>$2</em>');

        // 6) restaurar lo reservado
        return s.replace(/\u0000(\d+)\u0000/g, (m, i) => reservados[Number(i)]);
    }

    const RE = {
        cerco: /^\s*(```|~~~)\s*([\w+#.-]*)\s*$/,
        titulo: /^(#{1,6})\s+(.*?)\s*#*\s*$/,
        linea: /^\s*([-*_])(\s*\1){2,}\s*$/,
        cita: /^\s*>\s?(.*)$/,
        lista: /^(\s*)([-*+]|\d+[.)])\s+(.*)$/,
        separadorTabla: /^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)*\|?\s*$/
    };

    function celdas(fila) {
        let f = fila.trim();
        if (f.startsWith('|')) f = f.slice(1);
        if (f.endsWith('|')) f = f.slice(0, -1);
        return f.split('|').map(c => c.trim());
    }

    function esInicioDeBloque(lineas, i) {
        const l = lineas[i];
        return RE.cerco.test(l) || RE.titulo.test(l) || RE.linea.test(l) || RE.cita.test(l) || RE.lista.test(l)
            || (l.includes('|') && i + 1 < lineas.length && RE.separadorTabla.test(lineas[i + 1]));
    }

    function bloques(lineas, opciones) {
        const html = [];
        let i = 0;

        while (i < lineas.length) {
            const linea = lineas[i];

            if (!linea.trim()) { i++; continue; }

            // Bloque de código
            let m = linea.match(RE.cerco);
            if (m) {
                const cerca = m[1];
                const lenguaje = m[2];
                const codigo = [];
                i++;
                while (i < lineas.length && lineas[i].trim() !== cerca) {
                    codigo.push(lineas[i]);
                    i++;
                }
                i++; // saltar el cierre
                const clase = lenguaje ? ' class="lenguaje-' + escapar(lenguaje) + '"' : '';
                html.push('<pre><code' + clase + '>' + escapar(codigo.join('\n')) + '</code></pre>');
                continue;
            }

            // Título
            m = linea.match(RE.titulo);
            if (m) {
                const n = m[1].length;
                html.push('<h' + n + '>' + enLinea(m[2], opciones) + '</h' + n + '>');
                i++;
                continue;
            }

            // Línea horizontal
            if (RE.linea.test(linea)) {
                html.push('<hr>');
                i++;
                continue;
            }

            // Cita
            if (RE.cita.test(linea)) {
                const contenido = [];
                while (i < lineas.length && RE.cita.test(lineas[i])) {
                    contenido.push(lineas[i].match(RE.cita)[1]);
                    i++;
                }
                html.push('<blockquote>' + bloques(contenido, opciones) + '</blockquote>');
                continue;
            }

            // Tabla
            if (linea.includes('|') && i + 1 < lineas.length && RE.separadorTabla.test(lineas[i + 1])) {
                const encabezado = celdas(linea);
                const alineacion = celdas(lineas[i + 1]).map(c =>
                    c.startsWith(':') && c.endsWith(':') ? 'center' : c.endsWith(':') ? 'right' : '');
                const estilo = (k) => alineacion[k] ? ' style="text-align:' + alineacion[k] + '"' : '';
                i += 2;
                let t = '<table><thead><tr>' + encabezado.map((c, k) => '<th' + estilo(k) + '>' + enLinea(c, opciones) + '</th>').join('') + '</tr></thead><tbody>';
                while (i < lineas.length && lineas[i].includes('|') && lineas[i].trim()) {
                    t += '<tr>' + celdas(lineas[i]).map((c, k) => '<td' + estilo(k) + '>' + enLinea(c, opciones) + '</td>').join('') + '</tr>';
                    i++;
                }
                html.push(t + '</tbody></table>');
                continue;
            }

            // Lista (un nivel; las líneas indentadas se agregan al elemento anterior)
            m = linea.match(RE.lista);
            if (m) {
                const ordenada = /\d/.test(m[2]);
                const etiqueta = ordenada ? 'ol' : 'ul';
                const inicio = ordenada ? parseInt(m[2], 10) : 1;
                const elementos = [];
                while (i < lineas.length) {
                    const actual = lineas[i];
                    const lm = actual.match(RE.lista);
                    if (lm && /\d/.test(lm[2]) === ordenada) {
                        elementos.push(lm[3]);
                        i++;
                    } else if (actual.trim() && /^\s{2,}/.test(actual) && elementos.length) {
                        elementos[elementos.length - 1] += ' ' + actual.trim();
                        i++;
                    } else {
                        break;
                    }
                }
                const atributo = ordenada && inicio !== 1 ? ' start="' + inicio + '"' : '';
                html.push('<' + etiqueta + atributo + '>' + elementos.map(e => '<li>' + enLinea(e, opciones) + '</li>').join('') + '</' + etiqueta + '>');
                continue;
            }

            // Párrafo
            const parrafo = [];
            while (i < lineas.length && lineas[i].trim() && !(parrafo.length && esInicioDeBloque(lineas, i))) {
                parrafo.push(lineas[i]);
                i++;
            }
            const contenido = parrafo
                .map((l, k) => enLinea(l.trim(), opciones) + (k < parrafo.length - 1 ? (/ {2,}$/.test(l) ? '<br>' : ' ') : ''))
                .join('');
            html.push('<p>' + contenido + '</p>');
        }
        return html.join('\n');
    }

    /**
     * @param {string} texto  contenido markdown
     * @param {{resolverImagen?: function(string): string}} opciones
     */
    function renderizar(texto, opciones) {
        const op = Object.assign({ resolverImagen: (src) => src }, opciones || {});
        const lineas = String(texto || '').replace(/\r\n?/g, '\n').replace(/\t/g, '    ').split('\n');
        return bloques(lineas, op);
    }

    global.Markdown = { renderizar, escapar };
})(typeof window !== 'undefined' ? window : globalThis);
