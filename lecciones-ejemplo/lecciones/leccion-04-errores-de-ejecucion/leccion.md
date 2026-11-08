# Lección 4: Errores de ejecución

Un programa puede **compilar perfectamente** y aun así fallar **mientras se ejecuta**.
A eso se le llama **error de ejecución** (o *excepción*).

| Tipo de error   | ¿Cuándo aparece?        | ¿Quién lo detecta?       |
|-----------------|-------------------------|--------------------------|
| Compilación     | Antes de ejecutar       | El compilador `javac`    |
| Ejecución       | Mientras el programa corre | La máquina virtual `java` |

## Código a copiar

```java
void main() {
    int total = 100;
    int personas = 0;
    IO.println("Repartiendo " + total + " entre " + personas);
    int cadaUno = total / personas;
    IO.println("A cada uno le toca: " + cadaUno);
}
```

Dividir entre cero no está permitido con números enteros, así que Java lanzará:

```
Exception in thread "main" java.lang.ArithmeticException: / by zero
```

## Objetivo

1. **Compila** (no debe haber errores de compilación).
2. **Ejecuta** y observa el error de ejecución en la consola.
