# Lección 2: Errores de compilación

Un **error de compilación** ocurre cuando el código no respeta las reglas de escritura (sintaxis) de Java.
El programa **no llega a ejecutarse**: el compilador se detiene y te dice dónde está el problema.

## Código a copiar

Copia este código **tal como está**. Tiene un error a propósito: falta el punto y coma `;` al final de la línea.

```java
void main() {
    IO.println("Me falta algo al final")
}
```

## Cómo leer el error

El compilador mostrará algo como:

```
app25.java:2: error: ';' expected
```

- `app25.java:2` → el error está en la **línea 2**.
- `';' expected` → esperaba un punto y coma.

## Objetivo

Presiona **Compilar** y obtén **al menos un error de compilación**. Lee el mensaje con calma.
Después, si quieres, corrígelo y vuelve a compilar.
