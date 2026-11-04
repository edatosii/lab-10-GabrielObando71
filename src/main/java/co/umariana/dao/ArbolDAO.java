package co.umariana.dao;

import co.umariana.conexion.ConexionBD;
import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ResumenArbol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object): la única clase que habla con la base de datos. Guarda los árboles genealógicos
 * en las tablas "arboles" y "miembros" (ver db/schema.sql). Se construye igual que EstudianteDAO del laboratorio 9.
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 4)
 * ------------------------------------------
 * 1. Cada método marcado con "TODO" lanza UnsupportedOperationException. Reemplace esa línea por su
 *    implementación con JDBC.
 * 2. NO modifique las pruebas (src/test). Las pruebas NO necesitan MySQL: usan una base de datos H2 en memoria.
 * 3. Esta etapa usa las anteriores: para guardar recorra arbol.miembros(); para cargar, reconstruya el árbol con
 *    new ArbolGenealogico(sujeto) y arbol.agregar(...).
 *
 * REGLAS (las pruebas las verifican)
 *  - Los métodos NO lanzan SQLException: la capturan, imprimen el mensaje con System.out.println y devuelven
 *    un valor "de error" (-1, false, lista vacía o null).
 *  - Use SIEMPRE PreparedStatement con parámetros (?), nunca concatene datos dentro del SQL.
 *  - Guardar un árbol son VARIAS sentencias (el árbol y todos sus miembros): hágalo en una TRANSACCIÓN
 *    (con.setAutoCommit(false); ... con.commit(); y con.rollback() si algo falla) para que nunca quede
 *    guardado "a medias". Antes de cerrar la conexión vuelva a dejar setAutoCommit(true).
 *  - El id de cada fila lo genera la base de datos (AUTO_INCREMENT).
 *  - Los miembros se guardan en el orden de arbol.miembros(): así, al leerlos ORDER BY id, cada persona
 *    aparece después de la que necesita para poder agregarla al árbol.
 */
public class ArbolDAO {

    // =====================================================================
    // ETAPA 4.1. Guardar un árbol nuevo (INSERT en dos tablas, con transacción)
    // =====================================================================

    /**
     * Guarda un árbol nuevo: una fila en "arboles" (con el nombre) y una fila en "miembros" por cada persona.
     *
     * @return el id que la base de datos le dio al árbol; -1 si el nombre es nulo, está en blanco o tiene más
     *         de 100 caracteres, si el árbol es null, o si hubo un error (en ese caso NO queda nada guardado)
     */
    public long guardar(String nombre, ArbolGenealogico arbol) {
        // TODO Etapa 4.1: valide el nombre; abra la conexión y desactive el auto-commit;
        //   INSERT INTO arboles (nombre) VALUES (?)  -> use prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        //   y getGeneratedKeys() para leer el id;
        //   luego, por cada miembro: INSERT INTO miembros (arbol_id, posicion, nombre, nacimiento, fallecimiento)
        //   VALUES (?, ?, ?, ?, ?)  (fallecimiento null -> ps.setNull(5, java.sql.Types.INTEGER));
        //   commit; si hay un SQLException: rollback y devuelva -1.
        throw new UnsupportedOperationException("TODO: guardar");
    }

    // =====================================================================
    // ETAPA 4.2. Listar (SELECT con JOIN y COUNT)
    // =====================================================================

    /**
     * @return un resumen por cada árbol guardado (id, nombre, cuántos miembros tiene y cuándo se creó),
     *         ordenados por id; lista vacía si no hay árboles o si hay un error
     */
    public List<ResumenArbol> listar() {
        // TODO Etapa 4.2: SELECT a.id, a.nombre, a.creado, COUNT(m.id) AS total
        //                 FROM arboles a LEFT JOIN miembros m ON m.arbol_id = a.id
        //                 GROUP BY a.id, a.nombre, a.creado ORDER BY a.id
        //   (el LEFT JOIN hace que aparezcan también los árboles sin miembros, con total 0).
        //   Para la fecha use rs.getTimestamp("creado").toLocalDateTime().
        throw new UnsupportedOperationException("TODO: listar");
    }

    // =====================================================================
    // ETAPA 4.3. Cargar un árbol (SELECT y reconstrucción del árbol)
    // =====================================================================

    /**
     * @return el árbol con ese id, reconstruido con todos sus miembros; null si no existe ese id o si hay un error
     */
    public ArbolGenealogico cargar(long id) {
        // TODO Etapa 4.3: SELECT posicion, nombre, nacimiento, fallecimiento FROM miembros
        //                 WHERE arbol_id = ? ORDER BY id
        //   La primera fila es el sujeto: new ArbolGenealogico(sujeto); las demás: arbol.agregar(persona).
        //   Para leer un entero que puede ser NULL: int f = rs.getInt("fallecimiento"); y luego rs.wasNull().
        //   Si no hay filas, el árbol no existe (devuelva null).
        throw new UnsupportedOperationException("TODO: cargar");
    }

    // =====================================================================
    // ETAPA 4.4. Actualizar (reemplazar los miembros de un árbol, con transacción)
    // =====================================================================

    /**
     * Guarda los cambios de un árbol ya existente (por ejemplo, después de agregar o eliminar miembros):
     * deja en la base de datos exactamente los miembros de {@code arbol}. El nombre del árbol NO cambia.
     *
     * @return true si se actualizó; false si no existe un árbol con ese id, si el árbol es null o si hubo un
     *         error (en ese caso NO se modifica nada: ni se borran los miembros anteriores)
     */
    public boolean actualizar(long id, ArbolGenealogico arbol) {
        // TODO Etapa 4.4: en una transacción: compruebe que el árbol existe (SELECT id FROM arboles WHERE id = ?),
        //   DELETE FROM miembros WHERE arbol_id = ?  y vuelva a insertar los miembros de arbol.miembros().
        throw new UnsupportedOperationException("TODO: actualizar");
    }

    // =====================================================================
    // ETAPA 4.5. Eliminar (DELETE)
    // =====================================================================

    /**
     * Elimina el árbol y todos sus miembros (la base de datos borra los miembros sola: ON DELETE CASCADE).
     *
     * @return true si se eliminó; false si no existía ese id o si hubo un error
     */
    public boolean eliminar(long id) {
        // TODO Etapa 4.5: DELETE FROM arboles WHERE id = ?
        throw new UnsupportedOperationException("TODO: eliminar");
    }
}
