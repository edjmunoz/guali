/* Confirmación antes de eliminar un usuario administrador. */
document.querySelectorAll('form.confirmar-eliminar').forEach(function (form) {
    form.addEventListener('submit', function (e) {
        if (!window.confirm('¿Eliminar este usuario administrador?')) {
            e.preventDefault();
        }
    });
});
