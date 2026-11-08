# Lección 3: Variables y tipos de datos

Una **variable** es un espacio con nombre donde se guarda un valor. En Java cada variable tiene un **tipo**.

| Tipo      | Guarda               | Ejemplo                  |
|-----------|----------------------|--------------------------|
| `int`     | números enteros      | `int edad = 20;`         |
| `double`  | números con decimal  | `double nota = 85.5;`    |
| `boolean` | verdadero o falso    | `boolean aprobado = true;` |
| `String`  | texto                | `String nombre = "Ana";` |

## Código a copiar

```java
void main() {
    String nombre = "Ana";
    int edad = 20;
    double promedio = 85.5;
    boolean aprobado = promedio >= 61;

    IO.println("Nombre: " + nombre);
    IO.println("Edad: " + edad);
    IO.println("Promedio: " + promedio);
    IO.println("¿Aprobado? " + aprobado);
}
```

## Objetivo

Presiona **Compilar** y logra **0 errores**. Si aparece un error, revisa mayúsculas, comillas y puntos y comas.
