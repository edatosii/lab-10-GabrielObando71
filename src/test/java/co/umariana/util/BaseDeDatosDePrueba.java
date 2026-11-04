package co.umariana.util;

import co.umariana.conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Deja la base de datos de pruebas (H2 en memoria, con el mismo esquema que
 * MySQL: db/schema.sql) vacía y con los ids reiniciados en 1.
 * Se llama antes de cada prueba para que no se afecten entre sí.
 */
public final class BaseDeDatosDePrueba {

    private BaseDeDatosDePrueba() {
    }

    public static void limpiar() {
        try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM miembros");
            st.execute("DELETE FROM arboles");
            st.execute("ALTER TABLE miembros ALTER COLUMN id RESTART WITH 1");
            st.execute("ALTER TABLE arboles ALTER COLUMN id RESTART WITH 1");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo limpiar la base de datos de prueba", e);
        }
    }
}
