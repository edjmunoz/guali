# Lección 5: Condicionales con if / else

Con `if` el programa **toma decisiones**: ejecuta un bloque solo si una condición es verdadera.

## Código a copiar

Esta vez usaremos la forma clásica, declarando una clase pública:

```java
public class Notas {
    public static void main(String[] args) {
        int nota = 75;

        if (nota >= 90) {
            System.out.println("Excelente");
        } else if (nota >= 61) {
            System.out.println("Aprobado");
        } else {
            System.out.println("Reprobado");
        }
    }
}
```

## Reto extra

Cambia el valor de `nota` (por ejemplo `95` o `40`), compila y ejecuta de nuevo. ¿Qué cambia?

## Objetivo

Compila y **ejecuta** el programa sin errores.
