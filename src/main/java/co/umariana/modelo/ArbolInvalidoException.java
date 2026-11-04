package co.umariana.modelo;

/**
 * Se lanza cuando un árbol genealógico (o el texto con el que se construye) no cumple las reglas:
 * una posición repetida, un miembro sin su padre o hijo en el árbol, un sujeto que se quiere
 * eliminar, etc. El mensaje explica el problema y se muestra tal cual al usuario.
 *
 * Es una IllegalArgumentException, igual que los errores de validación de Persona.
 *
 * Esta clase YA está completa: no es necesario modificarla.
 */
public class ArbolInvalidoException extends IllegalArgumentException {

    public ArbolInvalidoException(String mensaje) {
        super(mensaje);
    }
}
