package co.umariana.dao;

import co.umariana.conexion.ConexionBD;
import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.Persona;
import co.umariana.modelo.ResumenArbol;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object): la única clase que habla con la base de datos.
 */
public class ArbolDAO {

    private Connection obtenerConexionSegura() throws SQLException {
        // Si el framework de pruebas define la URL de H2 en memoria, la usamos directamente
        String h2Url = System.getProperty("db.url");
        if (h2Url != null && !h2Url.isBlank()) {
            String h2User = System.getProperty("db.user");
            String h2Pass = System.getProperty("db.password");
            return DriverManager.getConnection(h2Url, h2User != null ? h2User : "", h2Pass != null ? h2Pass : "");
        }
        
        // De lo contrario, usamos la conexión estándar configurada en ConexionBD para la aplicación
        return ConexionBD.conectar();
    }

    public long guardar(String nombre, ArbolGenealogico arbol) {
        if (nombre == null || nombre.trim().isEmpty() || nombre.length() > 100 || arbol == null) {
            return -1;
        }

        String sqlArbol = "INSERT INTO arboles (nombre) VALUES (?)";
        String sqlMiembro = "INSERT INTO miembros (arbol_id, posicion, nombre, nacimiento, fallecimiento) VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = obtenerConexionSegura();
            if (con == null) {
                return -1;
            }

            con.setAutoCommit(false);

            long arbolId = -1;
            try (PreparedStatement psArbol = con.prepareStatement(sqlArbol, Statement.RETURN_GENERATED_KEYS)) {
                psArbol.setString(1, nombre.trim());
                int filasAfectadas = psArbol.executeUpdate();
                if (filasAfectadas == 0) {
                    con.rollback();
                    return -1;
                }

                try (ResultSet generatedKeys = psArbol.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        arbolId = generatedKeys.getLong(1);
                    } else {
                        con.rollback();
                        return -1;
                    }
                }
            }

            List<Persona> miembros = arbol.miembros();
            try (PreparedStatement psMiembro = con.prepareStatement(sqlMiembro)) {
                for (Persona p : miembros) {
                    psMiembro.setLong(1, arbolId);
                    psMiembro.setString(2, p.getPosicion());
                    psMiembro.setString(3, p.getNombre());
                    psMiembro.setInt(4, p.getNacimiento());
                    if (p.getFallecimiento() != null) {
                        psMiembro.setInt(5, p.getFallecimiento());
                    } else {
                        psMiembro.setNull(5, java.sql.Types.INTEGER);
                    }
                    psMiembro.addBatch();
                }
                psMiembro.executeBatch();
            }

            con.commit();
            return arbolId;

        } catch (SQLException e) {
            System.out.println("Error al guardar el árbol: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error en rollback: " + ex.getMessage());
                }
            }
            return -1;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.out.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    public List<ResumenArbol> listar() {
        List<ResumenArbol> resumenes = new ArrayList<>();
        String sql = "SELECT a.id, a.nombre, a.creado, COUNT(m.id) AS total " +
                     "FROM arboles a LEFT JOIN miembros m ON m.arbol_id = a.id " +
                     "GROUP BY a.id, a.nombre, a.creado ORDER BY a.id";

        try (Connection con = obtenerConexionSegura();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                int total = rs.getInt("total");
                java.time.LocalDateTime creado = rs.getTimestamp("creado").toLocalDateTime();

                ResumenArbol resumen = new ResumenArbol(id, nombre, total, creado);
                resumenes.add(resumen);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar los árboles: " + e.getMessage());
        }
        return resumenes;
    }

    public ArbolGenealogico cargar(long id) {
        String sql = "SELECT posicion, nombre, nacimiento, fallecimiento FROM miembros WHERE arbol_id = ? ORDER BY id";
        ArbolGenealogico arbol = null;

        try (Connection con = obtenerConexionSegura();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                boolean esPrimero = true;
                while (rs.next()) {
                    String posicion = rs.getString("posicion");
                    String nombre = rs.getString("nombre");
                    int nacimiento = rs.getInt("nacimiento");
                    int fallecimientoVal = rs.getInt("fallecimiento");
                    Integer fallecimiento = rs.wasNull() ? null : fallecimientoVal;

                    Persona persona = new Persona(posicion, nombre, nacimiento, fallecimiento);

                    if (esPrimero) {
                        arbol = new ArbolGenealogico(persona);
                        esPrimero = false;
                    } else {
                        arbol.agregar(persona);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar el árbol: " + e.getMessage());
            return null;
        }
        return arbol;
    }

    public boolean actualizar(long id, ArbolGenealogico arbol) {
        if (arbol == null) {
            return false;
        }

        String sqlCheck = "SELECT id FROM arboles WHERE id = ?";
        String sqlDelete = "DELETE FROM miembros WHERE arbol_id = ?";
        String sqlInsert = "INSERT INTO miembros (arbol_id, posicion, nombre, nacimiento, fallecimiento) VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = obtenerConexionSegura();
            if (con == null) {
                return false;
            }

            con.setAutoCommit(false);

            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setLong(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement psDelete = con.prepareStatement(sqlDelete)) {
                psDelete.setLong(1, id);
                psDelete.executeUpdate();
            }

            List<Persona> miembros = arbol.miembros();
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                for (Persona p : miembros) {
                    psInsert.setLong(1, id);
                    psInsert.setString(2, p.getPosicion());
                    psInsert.setString(3, p.getNombre());
                    psInsert.setInt(4, p.getNacimiento());
                    if (p.getFallecimiento() != null) {
                        psInsert.setInt(5, p.getFallecimiento());
                    } else {
                        psInsert.setNull(5, java.sql.Types.INTEGER);
                    }
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al actualizar el árbol: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error en rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.out.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    public boolean eliminar(long id) {
        String sql = "DELETE FROM arboles WHERE id = ?";

        try (Connection con = obtenerConexionSegura();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar el árbol: " + e.getMessage());
            return false;
        }
    }
}