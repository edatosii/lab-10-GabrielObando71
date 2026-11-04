package co.umariana.modelo;

import java.time.LocalDateTime;

/**
 * Una fila del listado de árboles guardados: su id, su nombre, cuántos miembros tiene y cuándo
 * se creó. Es lo que devuelve ArbolDAO.listar().
 *
 * Esta clase YA está completa: no es necesario modificarla.
 */
public record ResumenArbol(long id, String nombre, int totalMiembros, LocalDateTime creado) {
}
