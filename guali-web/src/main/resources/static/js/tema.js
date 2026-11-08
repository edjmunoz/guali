/* Aplica el tema y tamaño de fuente guardados antes de pintar la página (evita parpadeo). */
(function () {
    try {
        var p = JSON.parse(localStorage.getItem('guali.preferencias') || 'null');
        if (p) {
            document.documentElement.setAttribute('data-tema', p.tema === 'oscuro' ? 'oscuro' : 'claro');
            if (p.tamanoFuente) {
                document.documentElement.style.fontSize = p.tamanoFuente + 'px';
            }
        }
    } catch (e) {
        /* sin almacenamiento local: se usan los valores por defecto */
    }
})();
