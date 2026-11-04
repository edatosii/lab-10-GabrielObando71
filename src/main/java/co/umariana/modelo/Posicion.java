package co.umariana.modelo;

/**
 * Las posiciones de un árbol genealógico. Cada miembro tiene una posición (texto) que dice quién es
 * respecto al SUJETO (la persona de la que trata el árbol). Se usan dos sistemas:
 *
 * AHNENTAFEL (para los ASCENDIENTES: padres, abuelos...). Es un número entero:
 *
 *     1                  el sujeto
 *     2 = padre del 1    3 = madre del 1
 *     4 = padre del 2    5 = madre del 2    6 = padre del 3    7 = madre del 3
 *
 *   Regla: el padre de la persona n es 2n y la madre es 2n + 1. Por eso los números pares son
 *   padres y los impares (mayores que 1) son madres. Y la persona n es hijo/a de n / 2 (división entera).
 *
 * D'ABOVILLE (para los DESCENDIENTES: hijos, nietos...). Es el "1" del sujeto seguido de un punto y
 * el número de orden en cada generación:
 *
 *     1.1  primer hijo del sujeto      1.2  segundo hijo del sujeto
 *     1.1.1  primer hijo del 1.1       1.2.3  tercer hijo del 1.2
 *
 *   Regla: el progenitor de 1.2.3 es 1.2 (se quita el último número).
 *
 * La posición "1" (el sujeto) cuenta como Ahnentafel.
 *
 * LÍMITES (para que las posiciones quepan en la base de datos y en un int):
 *   - Ahnentafel: de 1 a 2047 (hasta 10 generaciones de ascendientes).
 *   - D'Aboville: cada número de orden va de 1 a 9999 y la posición completa tiene máximo 50 caracteres.
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 1)
 * ------------------------------------------
 * Cada método marcado con "TODO" lanza UnsupportedOperationException: reemplácelo por su implementación.
 * Los métodos reciben texto: use String.matches(...) (expresiones regulares) o String.split(...).
 * NO modifique las pruebas (src/test).
 */
public final class Posicion {

    /** La posición del sujeto. */
    public static final String SUJETO = "1";
    /** Mayor número Ahnentafel permitido (10 generaciones de ascendientes). */
    public static final int MAX_AHNENTAFEL = 2047;
    /** Mayor número de orden permitido en cada nivel d'Aboville. */
    public static final int MAX_ORDEN = 9999;
    /** Longitud máxima de una posición d'Aboville (es lo que cabe en la base de datos). */
    public static final int MAX_LONGITUD = 50;

    private Posicion() {
    }

    /**
     * @return true solo si la posición es exactamente "1"
     */
    public static boolean esSujeto(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: esSujeto");
    }

    /**
     * Ahnentafel: un entero positivo SIN ceros a la izquierda, de 1 a MAX_AHNENTAFEL, sin signo,
     * espacios ni puntos. Incluye el "1" del sujeto.
     * Ejemplos válidos: "1", "2", "13", "2047". Inválidos: null, "", "0", "01", "-2", "2.0", " 2", "2048", "abc".
     */
    public static boolean esAhnentafel(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: esAhnentafel");
    }

    /**
     * D'Aboville: empieza por "1" y sigue con UNO o más grupos ".k", donde k es un entero positivo sin ceros
     * a la izquierda, de 1 a MAX_ORDEN. La posición completa no puede pasar de MAX_LONGITUD caracteres.
     * Ejemplos válidos: "1.1", "1.2.3", "1.9999". Inválidos: null, "1" (ese es el sujeto), "2.1", "1.", "1..1",
     * "1.0", "1.01", "1.10000", ".1", "1.1.", "1.a".
     */
    public static boolean esDAboville(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: esDAboville");
    }

    /**
     * @return true si es Ahnentafel o d'Aboville (ver esAhnentafel y esDAboville)
     */
    public static boolean esValida(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: esValida");
    }

    /**
     * @return true si es un ASCENDIENTE: Ahnentafel válida distinta de "1"
     */
    public static boolean esAscendiente(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: esAscendiente");
    }

    /**
     * @return true si es un DESCENDIENTE: d'Aboville válida (el sujeto "1" NO es descendiente de sí mismo)
     */
    public static boolean esDescendiente(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: esDescendiente");
    }

    /**
     * Posición del PADRE de la persona Ahnentafel n: 2n. Ej.: "1" -> "2", "5" -> "10".
     * El resultado puede pasar de MAX_AHNENTAFEL (úselo con esAhnentafel para saber si cabe).
     *
     * @throws IllegalArgumentException si la posición no es Ahnentafel válida
     */
    public static String posicionDelPadre(String ahnentafel) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: posicionDelPadre");
    }

    /**
     * Posición de la MADRE de la persona Ahnentafel n: 2n + 1. Ej.: "1" -> "3", "5" -> "11".
     *
     * @throws IllegalArgumentException si la posición no es Ahnentafel válida
     */
    public static String posicionDeLaMadre(String ahnentafel) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: posicionDeLaMadre");
    }

    /**
     * Posición del HIJO/A (el que está en la línea hacia el sujeto) de un ascendiente: n / 2.
     * Ej.: "2" -> "1", "3" -> "1", "5" -> "2", "11" -> "5".
     *
     * @throws IllegalArgumentException si no es un ascendiente (el sujeto "1" no tiene hijo en la línea)
     */
    public static String posicionDelHijoEnLinea(String ahnentafel) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: posicionDelHijoEnLinea");
    }

    /**
     * Posición del PROGENITOR de un descendiente: la misma posición sin el último número.
     * Ej.: "1.2" -> "1", "1.2.3" -> "1.2".
     *
     * @throws IllegalArgumentException si no es un descendiente (d'Aboville válida)
     */
    public static String posicionDelProgenitor(String daboville) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: posicionDelProgenitor");
    }

    /**
     * Número de orden de un descendiente entre sus hermanos: el último número. Ej.: "1.2.3" -> 3.
     *
     * @throws IllegalArgumentException si no es un descendiente (d'Aboville válida)
     */
    public static int orden(String daboville) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: orden");
    }

    /**
     * Generaciones de distancia al sujeto.
     * Ascendientes (Ahnentafel): "1" -> 0; "2" y "3" -> 1; "4" a "7" -> 2; "8" a "15" -> 3 (es la parte entera
     * del logaritmo en base 2). Descendientes (d'Aboville): "1.1" -> 1; "1.2.3" -> 2 (cantidad de puntos).
     *
     * @throws IllegalArgumentException si la posición no es válida
     */
    public static int generacion(String posicion) {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: generacion");
    }
}
