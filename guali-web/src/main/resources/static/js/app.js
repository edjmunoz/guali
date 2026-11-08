/*
 * Guali Dev — lógica de la pantalla principal.
 * Se comunica con el servidor por la API REST (/api/...).
 */
(function () {
    'use strict';

    // ------------------------------------------------------------------ utilidades
    const $ = (sel) => document.querySelector(sel);
    const meta = (nombre) => (document.querySelector('meta[name="' + nombre + '"]') || {}).content;
    const CSRF_TOKEN = meta('_csrf');
    const CSRF_HEADER = meta('_csrf_header') || 'X-CSRF-TOKEN';
    const ES_ADMIN = meta('es-admin') === 'true';

    const TAMANO_MIN = 12;
    const TAMANO_MAX = 28;
    const TAMANO_NORMAL = 16;

    const NOMBRES_OBJETIVO = {
        COMPILAR_CON_ERROR: 'Compilar con error',
        COMPILAR_EXITOSO: 'Compilar exitoso',
        EJECUTAR_CON_ERROR: 'Ejecutar con error',
        EJECUTAR_EXITOSO: 'Ejecutar exitoso'
    };

    /** Llamada a la API con CSRF y manejo de errores ({ mensaje }). */
    async function api(url, opciones = {}) {
        const cabeceras = { 'Accept': 'application/json' };
        let cuerpo = opciones.body;
        const metodo = opciones.method || 'GET';
        if (metodo !== 'GET') {
            cabeceras[CSRF_HEADER] = CSRF_TOKEN;
        }
        if (cuerpo !== undefined && !(cuerpo instanceof FormData)) {
            cabeceras['Content-Type'] = 'application/json';
            cuerpo = JSON.stringify(cuerpo);
        }
        let resp;
        try {
            resp = await fetch(url, {
                method: metodo,
                headers: cabeceras,
                body: cuerpo,
                credentials: 'same-origin',
                keepalive: !!opciones.keepalive
            });
        } catch (e) {
            throw new Error('No hay conexión con el servidor.');
        }
        const texto = await resp.text();
        let datos = null;
        if (texto) {
            try { datos = JSON.parse(texto); } catch (e) { datos = null; }
        }
        if (!resp.ok) {
            if (resp.status === 401) throw new Error('Tu sesión de administrador expiró. Vuelve a iniciar sesión.');
            if (resp.status === 403) throw new Error((datos && datos.mensaje) || 'No tienes permiso (recarga la página).');
            throw new Error((datos && datos.mensaje) || ('Error ' + resp.status));
        }
        return datos;
    }

    function aviso(texto, tipo = 'info', ms = 4500) {
        const div = document.createElement('div');
        div.className = 'aviso-toast ' + tipo;
        div.textContent = texto;
        $('#avisos').appendChild(div);
        setTimeout(() => div.remove(), ms);
    }

    function esc(s) {
        return Markdown.escapar(s == null ? '' : s);
    }

    // ------------------------------------------------------------------ estado
    const estado = {
        sesionId: null,
        leccion: null,
        codigoCompilado: null,   // código de la última compilación exitosa
        ocupado: false,
        preferencias: { tamanoFuente: TAMANO_NORMAL, tema: 'claro' }
    };

    const editor = $('#editor');
    const numeros = $('#numeros');
    const consola = $('#consola');
    const btnCompilar = $('#btn-compilar');
    const btnEjecutar = $('#btn-ejecutar');

    // ------------------------------------------------------------------ consola
    function escribir(texto, clase) {
        const span = document.createElement('span');
        if (clase) span.className = clase;
        span.textContent = texto.endsWith('\n') ? texto : texto + '\n';
        consola.appendChild(span);
        consola.scrollTop = consola.scrollHeight;
    }

    function hora() {
        return new Date().toLocaleTimeString('es-GT', { hour12: false });
    }

    // ------------------------------------------------------------------ menús
    function cerrarMenus(excepto) {
        document.querySelectorAll('.menu-lista.abierto').forEach(m => {
            if (m !== excepto) {
                m.classList.remove('abierto');
                m.previousElementSibling.setAttribute('aria-expanded', 'false');
            }
        });
    }

    document.querySelectorAll('.menu-boton').forEach(boton => {
        boton.addEventListener('click', (e) => {
            e.stopPropagation();
            const lista = boton.nextElementSibling;
            const abrir = !lista.classList.contains('abierto');
            cerrarMenus(lista);
            lista.classList.toggle('abierto', abrir);
            boton.setAttribute('aria-expanded', String(abrir));
            if (abrir) {
                const primero = lista.querySelector('button');
                if (primero) primero.focus();
            }
        });
    });
    document.addEventListener('click', () => cerrarMenus());
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') cerrarMenus(); });

    // Todos los elementos con data-accion
    const acciones = {
        'abrir-leccion': abrirDialogoLecciones,
        'cerrar-leccion': () => cerrarLeccion(true),
        'fuente-mas': () => cambiarFuente(+2),
        'fuente-menos': () => cambiarFuente(-2),
        'fuente-normal': () => cambiarFuente(0),
        'tema-claro': () => cambiarTema('claro'),
        'tema-oscuro': () => cambiarTema('oscuro'),
        'historial': abrirHistorial,
        'ayuda': () => $('#dlg-ayuda').showModal(),
        'acerca': () => $('#dlg-acerca').showModal(),
        'importar': abrirImportar,
        'exportar': exportar,
        'eliminar': abrirEliminar
    };

    document.addEventListener('click', (e) => {
        const el = e.target.closest('[data-accion]');
        if (!el) return;
        const accion = acciones[el.dataset.accion];
        if (accion) {
            cerrarMenus();
            accion();
        }
    });

    // Diálogos: botones [data-cerrar] y clic fuera del contenido
    document.querySelectorAll('dialog').forEach(d => {
        d.addEventListener('click', (e) => {
            if (e.target === d || e.target.closest('[data-cerrar]')) d.close();
        });
    });

    function confirmar(texto) {
        return new Promise((resolver) => {
            const dlg = $('#dlg-confirmar');
            $('#texto-confirmar').textContent = texto;
            const si = $('#confirmar-si');
            const no = $('#confirmar-no');
            const terminar = (valor) => {
                si.onclick = no.onclick = null;
                dlg.onclose = null;
                if (dlg.open) dlg.close();
                resolver(valor);
            };
            si.onclick = () => terminar(true);
            no.onclick = () => terminar(false);
            dlg.onclose = () => terminar(false);
            dlg.showModal();
        });
    }

    // ------------------------------------------------------------------ preferencias
    function aplicarPreferencias() {
        const p = estado.preferencias;
        document.documentElement.setAttribute('data-tema', p.tema);
        document.documentElement.style.fontSize = p.tamanoFuente + 'px';
        try { localStorage.setItem('guali.preferencias', JSON.stringify(p)); } catch (e) { /* ignorar */ }
    }

    let temporizadorPreferencias = null;
    function guardarPreferencias() {
        clearTimeout(temporizadorPreferencias);
        temporizadorPreferencias = setTimeout(() => {
            api('/api/preferencias', { method: 'PUT', body: estado.preferencias })
                .catch(err => aviso('No se pudo guardar la configuración: ' + err.message, 'error'));
        }, 400);
    }

    function cambiarFuente(delta) {
        const p = estado.preferencias;
        p.tamanoFuente = delta === 0 ? TAMANO_NORMAL
            : Math.max(TAMANO_MIN, Math.min(TAMANO_MAX, p.tamanoFuente + delta));
        aplicarPreferencias();
        guardarPreferencias();
        aviso('Tamaño de fuente: ' + p.tamanoFuente + ' px', 'info', 1500);
    }

    function cambiarTema(tema) {
        estado.preferencias.tema = tema;
        aplicarPreferencias();
        guardarPreferencias();
    }

    async function cargarPreferencias() {
        try {
            const p = await api('/api/preferencias');
            if (p) {
                estado.preferencias = { tamanoFuente: p.tamanoFuente || TAMANO_NORMAL, tema: p.tema || 'claro' };
                aplicarPreferencias();
            }
        } catch (e) {
            /* se mantienen las preferencias locales */
        }
    }

    // ------------------------------------------------------------------ editor
    function actualizarNumeros() {
        const total = editor.value.split('\n').length;
        let texto = '';
        for (let i = 1; i <= total; i++) texto += i + '\n';
        numeros.textContent = texto;
        numeros.scrollTop = editor.scrollTop;
    }

    function actualizarBotones() {
        btnCompilar.disabled = estado.ocupado;
        btnEjecutar.disabled = estado.ocupado || estado.codigoCompilado === null || estado.codigoCompilado !== editor.value;
    }

    editor.addEventListener('input', () => {
        actualizarNumeros();
        actualizarBotones();
    });
    editor.addEventListener('scroll', () => { numeros.scrollTop = editor.scrollTop; });

    // Bloquear pegar / arrastrar: el estudiante debe escribir el código a mano
    ['paste', 'drop'].forEach(evento => editor.addEventListener(evento, (e) => {
        e.preventDefault();
        aviso('Pegar está deshabilitado: escribe el código a mano. ¡Así se aprende! 💪', 'aviso');
    }));

    editor.addEventListener('keydown', (e) => {
        // Tab inserta 4 espacios
        if (e.key === 'Tab' && !e.shiftKey) {
            e.preventDefault();
            insertarTexto('    ');
            return;
        }
        // Enter conserva la sangría de la línea anterior
        if (e.key === 'Enter' && !e.ctrlKey && !e.metaKey) {
            const inicio = editor.selectionStart;
            const anterior = editor.value.slice(0, inicio).split('\n').pop();
            let sangria = (anterior.match(/^\s*/) || [''])[0];
            if (/\{\s*$/.test(anterior)) sangria += '    ';
            e.preventDefault();
            insertarTexto('\n' + sangria);
            return;
        }
        // "}" al inicio de una línea quita un nivel de sangría
        if (e.key === '}' && editor.selectionStart === editor.selectionEnd) {
            const inicio = editor.selectionStart;
            const linea = editor.value.slice(0, inicio).split('\n').pop();
            if (/^ {4,}$/.test(linea)) {
                e.preventDefault();
                editor.setRangeText('}', inicio - 4, inicio, 'end');
                editor.dispatchEvent(new Event('input'));
                return;
            }
        }
        // Ctrl+Enter compila
        if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
            e.preventDefault();
            compilar();
        }
    });

    function insertarTexto(texto) {
        const inicio = editor.selectionStart;
        const fin = editor.selectionEnd;
        editor.setRangeText(texto, inicio, fin, 'end');
        editor.dispatchEvent(new Event('input'));
    }

    // ------------------------------------------------------------------ lecciones
    async function abrirDialogoLecciones() {
        const dlg = $('#dlg-abrir');
        const lista = $('#lista-abrir');
        lista.innerHTML = '<li class="texto-suave cargando">Cargando lecciones</li>';
        $('#buscar-leccion').value = '';
        dlg.showModal();
        try {
            const lecciones = await api('/api/lecciones');
            pintarListaAbrir(lecciones);
        } catch (e) {
            lista.innerHTML = '<li>' + esc(e.message) + '</li>';
        }
    }

    function pintarListaAbrir(lecciones) {
        const lista = $('#lista-abrir');
        if (!lecciones.length) {
            lista.innerHTML = '<li class="texto-suave">No hay lecciones disponibles.'
                + (ES_ADMIN ? ' Importa un compendio desde Configuración.' : ' Pide al administrador que importe lecciones.')
                + '</li>';
            return;
        }
        lista.innerHTML = lecciones.map(l =>
            '<li class="seleccionable" tabindex="0" data-id="' + l.id + '" data-texto="' + esc(l.titulo.toLowerCase()) + '">'
            + '<span aria-hidden="true">📄</span>'
            + '<div class="info-leccion"><div class="nombre">' + esc(l.titulo) + '</div>'
            + '<div class="desc">🎯 ' + esc(NOMBRES_OBJETIVO[l.objetivo] || l.objetivo) + ' — ' + esc(l.descripcionObjetivo) + '</div></div>'
            + (estado.leccion && estado.leccion.id === l.id ? '<span class="insignia">Abierta</span>' : '')
            + '</li>').join('');
    }

    $('#buscar-leccion').addEventListener('input', (e) => {
        const q = e.target.value.trim().toLowerCase();
        document.querySelectorAll('#lista-abrir li[data-id]').forEach(li => {
            li.classList.toggle('oculto', !!q && !li.dataset.texto.includes(q));
        });
    });

    function seleccionarLeccion(e) {
        const li = e.target.closest('li[data-id]');
        if (!li) return;
        if (e.type === 'keydown' && e.key !== 'Enter') return;
        $('#dlg-abrir').close();
        abrirLeccion(Number(li.dataset.id));
    }
    $('#lista-abrir').addEventListener('click', seleccionarLeccion);
    $('#lista-abrir').addEventListener('keydown', seleccionarLeccion);

    async function abrirLeccion(id) {
        if (estado.sesionId && editor.value.trim()) {
            const ok = await confirmar('Se cerrará la lección actual y se limpiará el editor. ¿Continuar?');
            if (!ok) return;
        }
        try {
            const sesion = await api('/api/sesiones', { method: 'POST', body: { leccionId: id } });
            estado.sesionId = sesion.sesionId;
            estado.leccion = sesion.leccion;
            mostrarLeccion(sesion.leccion);
            limpiarEditor();
            consola.textContent = '';
            escribir('[' + hora() + '] Lección abierta: ' + sesion.leccion.titulo, 'info');
            escribir('🎯 Objetivo: ' + sesion.leccion.descripcionObjetivo, 'meta');
            editor.focus();
        } catch (e) {
            aviso(e.message, 'error');
        }
    }

    function mostrarLeccion(l) {
        $('#titulo-activo').textContent = l.titulo;
        const insignia = $('#insignia-objetivo');
        insignia.textContent = '🎯 ' + (NOMBRES_OBJETIVO[l.objetivo] || l.objetivo);
        insignia.title = l.descripcionObjetivo;
        insignia.classList.remove('oculto');
        mostrarEstado('Pendiente');

        const base = '/api/lecciones/' + l.id + '/recursos/';
        $('#contenido-leccion').innerHTML = Markdown.renderizar(l.contenidoMd, {
            resolverImagen: (src) => /^(https?:|data:|\/|#)/i.test(src)
                ? src
                : base + src.replace(/^\.\//, '').split('/').map(encodeURIComponent).join('/')
        });
        $('#contenido-leccion').scrollTop = 0;
    }

    function mostrarEstado(texto) {
        const e = $('#insignia-estado');
        if (!texto) {
            e.classList.add('oculto');
            return;
        }
        e.textContent = texto === 'Completada' ? '✅ Completada' : '⏳ Pendiente';
        e.className = 'estado ' + (texto === 'Completada' ? 'completada' : 'pendiente');
    }

    async function cerrarLeccion(preguntar) {
        if (!estado.sesionId) {
            aviso('No hay ninguna lección abierta.', 'info');
            return;
        }
        if (preguntar && editor.value.trim()) {
            const ok = await confirmar('¿Cerrar la lección? Se limpiará el editor.');
            if (!ok) return;
        }
        const id = estado.sesionId;
        try {
            await api('/api/sesiones/' + id + '/cerrar', { method: 'POST' });
        } catch (e) {
            aviso(e.message, 'error');
        }
        reiniciarVista();
        escribir('[' + hora() + '] Lección cerrada.', 'meta');
    }

    function reiniciarVista() {
        estado.sesionId = null;
        estado.leccion = null;
        $('#titulo-activo').textContent = 'Ninguna lección abierta';
        $('#insignia-objetivo').classList.add('oculto');
        mostrarEstado(null);
        $('#contenido-leccion').innerHTML =
            '<div class="vacio"><div><div class="icono">📚</div><p>No hay ninguna lección abierta.</p>'
            + '<button class="btn primario" data-accion="abrir-leccion">Abrir una lección</button></div></div>';
        limpiarEditor();
    }

    function limpiarEditor() {
        editor.value = '';
        estado.codigoCompilado = null;
        actualizarNumeros();
        actualizarBotones();
    }

    // Si el estudiante sale de la página, la lección queda como abandonada (fecha final)
    window.addEventListener('pagehide', () => {
        if (estado.sesionId) {
            api('/api/sesiones/' + estado.sesionId + '/cerrar', { method: 'POST', keepalive: true }).catch(() => {});
        }
    });

    // ------------------------------------------------------------------ compilar / ejecutar
    function ocupar(valor, texto) {
        estado.ocupado = valor;
        const etiqueta = $('#estado-accion');
        etiqueta.textContent = texto || '';
        etiqueta.classList.toggle('cargando', valor);
        actualizarBotones();
    }

    function objetivoLogrado(resultado) {
        if (resultado.estado) mostrarEstado(resultado.estado);
        if (resultado.objetivoCumplido) {
            escribir('🏆 ¡Objetivo cumplido! La lección quedó marcada como COMPLETADA.', 'logro');
            aviso('🏆 ¡Lección completada!', 'ok', 6000);
        }
    }

    async function compilar() {
        if (estado.ocupado) return;
        const codigo = editor.value;
        if (!codigo.trim()) {
            aviso('El editor está vacío. Escribe tu código primero.', 'aviso');
            return;
        }
        ocupar(true, 'Compilando');
        escribir('\n[' + hora() + '] $ javac  (compilando…)', 'info');
        try {
            const r = await api('/api/codigo/compilar', {
                method: 'POST',
                body: { sesionId: estado.sesionId, codigo: codigo }
            });
            if (r.exitoso) {
                estado.codigoCompilado = codigo;
                escribir('✔ Compilación exitosa: 0 errores. Ya puedes presionar Ejecutar.', 'ok');
            } else {
                estado.codigoCompilado = null;
                escribir(r.salida || 'Error de compilación', 'error');
                const errores = (r.salida.match(/error:/g) || []).length;
                escribir('✘ Compilación con ' + (errores || 'al menos 1') + ' error(es). Lee el mensaje: indica la línea del problema.', 'aviso');
            }
            objetivoLogrado(r);
        } catch (e) {
            escribir('⚠ ' + e.message, 'error');
        } finally {
            ocupar(false);
        }
    }

    async function ejecutar() {
        if (estado.ocupado || estado.codigoCompilado === null) return;
        ocupar(true, 'Ejecutando');
        escribir('\n[' + hora() + '] $ java  (ejecutando…)', 'info');
        try {
            const r = await api('/api/codigo/ejecutar', {
                method: 'POST',
                body: { sesionId: estado.sesionId, codigo: estado.codigoCompilado }
            });
            if (!r.compilacionExitosa) {
                escribir(r.salidaCompilacion, 'error');
                estado.codigoCompilado = null;
            } else {
                if (r.salida) escribir(r.salida);
                if (r.errores) escribir(r.errores, 'error');
                if (r.timeout) {
                    escribir('⏱ El programa tardó demasiado y fue detenido (límite de tiempo).', 'aviso');
                } else if (r.exitoso) {
                    escribir('✔ El programa terminó correctamente.', 'ok');
                } else {
                    escribir('✘ El programa terminó con un error de ejecución.', 'aviso');
                }
            }
            objetivoLogrado(r);
        } catch (e) {
            escribir('⚠ ' + e.message, 'error');
        } finally {
            ocupar(false);
        }
    }

    btnCompilar.addEventListener('click', compilar);
    btnEjecutar.addEventListener('click', ejecutar);
    $('#btn-limpiar').addEventListener('click', () => { consola.textContent = ''; });

    // ------------------------------------------------------------------ historial
    async function abrirHistorial() {
        const cuerpo = $('#tabla-historial');
        cuerpo.innerHTML = '<tr><td colspan="4" class="cargando">Cargando</td></tr>';
        $('#dlg-historial').showModal();
        try {
            const filas = await api('/api/historial');
            if (!filas.length) {
                cuerpo.innerHTML = '<tr><td colspan="4" class="texto-suave">Aún no has abierto ninguna lección.</td></tr>';
                return;
            }
            cuerpo.innerHTML = filas.map(f =>
                '<tr><td>' + esc(f.leccion)
                + '<div class="texto-suave" style="font-size:.78rem">Compilaciones: ' + f.intentosCompilar
                + ' · Ejecuciones: ' + f.intentosEjecutar + '</div></td>'
                + '<td>' + esc(f.fechaInicio) + '</td>'
                + '<td>' + esc(f.fechaFin) + '</td>'
                + '<td><span class="estado ' + (f.estado === 'Completada' ? 'completada">✅ Completada' : 'pendiente">⏳ Pendiente (incompleta)') + '</span></td></tr>'
            ).join('');
        } catch (e) {
            cuerpo.innerHTML = '<tr><td colspan="4">' + esc(e.message) + '</td></tr>';
        }
    }

    // ------------------------------------------------------------------ administración de lecciones
    function abrirImportar() {
        if (!ES_ADMIN) return;
        $('#form-importar').reset();
        $('#resultado-importar').innerHTML = '';
        $('#dlg-importar').showModal();
    }

    if (ES_ADMIN) {
        $('#form-importar').addEventListener('submit', async (e) => {
            e.preventDefault();
            const archivo = $('#archivo-zip').files[0];
            if (!archivo) return;
            const boton = $('#btn-importar');
            const salida = $('#resultado-importar');
            boton.disabled = true;
            salida.innerHTML = '<span class="cargando">Importando</span>';
            const datos = new FormData();
            datos.append('archivo', archivo);
            try {
                const r = await api('/api/admin/lecciones/importar', { method: 'POST', body: datos });
                let html = '<div class="alerta ok">';
                if (r.nuevas.length) html += '<strong>Nuevas (' + r.nuevas.length + '):</strong> ' + r.nuevas.map(esc).join(', ') + '<br>';
                if (r.actualizadas.length) html += '<strong>Actualizadas (' + r.actualizadas.length + '):</strong> ' + r.actualizadas.map(esc).join(', ');
                html += '</div>';
                if (r.advertencias.length) {
                    html += '<div class="alerta error" style="margin-top:.5rem"><strong>Advertencias:</strong><ul style="margin:.3rem 0 0">'
                        + r.advertencias.map(a => '<li>' + esc(a) + '</li>').join('') + '</ul></div>';
                }
                salida.innerHTML = html;
                aviso('Importación terminada', 'ok');
            } catch (err) {
                salida.innerHTML = '<div class="alerta error">' + esc(err.message) + '</div>';
            } finally {
                boton.disabled = false;
            }
        });

        $('#lista-eliminar').addEventListener('click', async (e) => {
            const boton = e.target.closest('button[data-eliminar]');
            if (!boton) return;
            const id = Number(boton.dataset.eliminar);
            const titulo = boton.dataset.titulo;
            const ok = await confirmar('¿Eliminar definitivamente la lección "' + titulo + '"?');
            if (!ok) return;
            try {
                const r = await api('/api/admin/lecciones/' + id, { method: 'DELETE' });
                aviso(r.mensaje, 'ok');
                if (estado.leccion && estado.leccion.id === id) {
                    await cerrarLeccion(false);
                }
                abrirEliminar();
            } catch (err) {
                aviso(err.message, 'error');
            }
        });
    }

    function exportar() {
        if (!ES_ADMIN) return;
        window.location.href = '/api/admin/lecciones/exportar';
        aviso('Generando el archivo .zip…', 'info');
    }

    async function abrirEliminar() {
        if (!ES_ADMIN) return;
        const dlg = $('#dlg-eliminar');
        const lista = $('#lista-eliminar');
        if (!dlg.open) dlg.showModal();
        lista.innerHTML = '<li class="cargando">Cargando</li>';
        try {
            const lecciones = await api('/api/lecciones');
            lista.innerHTML = lecciones.length
                ? lecciones.map(l => '<li><div class="info-leccion"><div class="nombre">' + esc(l.titulo) + '</div>'
                    + '<div class="desc">' + esc(NOMBRES_OBJETIVO[l.objetivo] || l.objetivo) + '</div></div>'
                    + '<button class="btn peligro chico" data-eliminar="' + l.id + '" data-titulo="' + esc(l.titulo) + '">Eliminar</button></li>').join('')
                : '<li class="texto-suave">No hay lecciones.</li>';
        } catch (e) {
            lista.innerHTML = '<li>' + esc(e.message) + '</li>';
        }
    }

    // ------------------------------------------------------------------ atajos globales
    document.addEventListener('keydown', (e) => {
        if (e.key === 'F1') {
            e.preventDefault();
            $('#dlg-ayuda').showModal();
        } else if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'o') {
            e.preventDefault();
            abrirDialogoLecciones();
        }
    });

    // ------------------------------------------------------------------ inicio
    try {
        const local = JSON.parse(localStorage.getItem('guali.preferencias') || 'null');
        if (local) estado.preferencias = Object.assign(estado.preferencias, local);
    } catch (e) { /* ignorar */ }
    aplicarPreferencias();
    cargarPreferencias();
    actualizarNumeros();
    actualizarBotones();
    escribir('Bienvenido a Guali Dev. Abre una lección desde el menú "Lección" para comenzar.', 'meta');
})();
