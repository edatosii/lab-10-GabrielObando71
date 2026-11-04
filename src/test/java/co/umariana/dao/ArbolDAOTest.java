package co.umariana.dao;

import static org.junit.jupiter.api.Assertions.*;

import co.umariana.conexion.ConexionBD;
import co.umariana.construccion.ConstructorArbol;
import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.Persona;
import co.umariana.modelo.ResumenArbol;
import co.umariana.util.BaseDeDatosDePrueba;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la PERSISTENCIA (ArbolDAO) contra una base H2 en memoria (no necesitan MySQL).
 * NO MODIFIQUE ESTE ARCHIVO: haga que pasen escribiendo código en ArbolDAO.java.
 *
 * Para que estas pruebas puedan funcionar se asume que ArbolGenealogico ya está implementado (Etapa 2).
 *
 * Ejecutar una etapa:   ./scripts/test.sh -Dtest='ArbolDAOTest$Etapa1_Guardar'
 */
class ArbolDAOTest {

    static final String TEXTO = "1;John Doe;2002;\n"
            + "2;Henry Doe;1970;2010\n"
            + "3;Maria Doe;1972;\n"
            + "6;Luis Roa;1940;\n"
            + "1.1;Timmy Doe;2021;\n"
            + "1.2;Amy Doe;2026;\n"
            + "1.1.1;Baby Doe;2045;\n";

    ArbolDAO dao;
    ArbolGenealogico arbol;

    @BeforeEach
    void preparar() {
        BaseDeDatosDePrueba.limpiar();
        dao = new ArbolDAO();
        arbol = ConstructorArbol.construir(TEXTO);
    }

    private static List<String> posiciones(ArbolGenealogico a) {
        return a.miembros().stream().map(Persona::getPosicion).collect(Collectors.toList());
    }

    private static int contar(String tabla) {
        try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nested
    @DisplayName("Etapa 4.1 - Guardar")
    class Etapa1_Guardar {
        @Test void devuelveElIdGenerado() {
            assertEquals(1, dao.guardar("Familia Doe", arbol));
            assertEquals(2, dao.guardar("Otra", arbol));
        }

        @Test void guardaElArbolYTodosSusMiembros() {
            long id = dao.guardar("Familia Doe", arbol);
            assertEquals(1, contar("arboles"));
            assertEquals(7, contar("miembros"));
            assertTrue(id > 0);
        }

        @Test void guardaElFallecimientoNuloComoNull() throws SQLException {
            dao.guardar("Familia Doe", arbol);
            try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT posicion, fallecimiento FROM miembros ORDER BY id")) {
                int conFallecimiento = 0;
                while (rs.next()) {
                    int f = rs.getInt("fallecimiento");
                    if (!rs.wasNull()) {
                        conFallecimiento++;
                        assertEquals("2", rs.getString("posicion"));
                        assertEquals(2010, f);
                    }
                }
                assertEquals(1, conFallecimiento);
            }
        }

        @Test void nombreInvalidoNoGuardaNada() {
            assertEquals(-1, dao.guardar(null, arbol));
            assertEquals(-1, dao.guardar("", arbol));
            assertEquals(-1, dao.guardar("   ", arbol));
            assertEquals(-1, dao.guardar("a".repeat(101), arbol));
            assertEquals(-1, dao.guardar("Familia", null));
            assertEquals(0, contar("arboles"));
            assertEquals(0, contar("miembros"));
        }

        @Test void nombreDe100CaracteresSeGuarda() {
            assertTrue(dao.guardar("a".repeat(100), arbol) > 0);
        }

        @Test void usaParametrosYNoConcatenaElNombre() {
            String malicioso = "x'); DROP TABLE miembros; --";
            long id = dao.guardar(malicioso, arbol);
            assertTrue(id > 0);
            assertEquals(7, contar("miembros"));
            assertEquals(malicioso, dao.listar().get(0).nombre());
        }

        @Test void siFallaUnMiembroNoQuedaNadaGuardado() throws SQLException {
            // un trigger-less forzado: eliminamos la tabla de miembros para que el INSERT de los miembros falle
            try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement()) {
                st.execute("ALTER TABLE miembros ADD CONSTRAINT ck_prueba CHECK (nacimiento < 2040)");
            }
            try {
                // el "1.1.1" nace en 2045: viola la restricción a la mitad de la transacción
                assertEquals(-1, dao.guardar("Familia Doe", arbol));
                assertEquals(0, contar("arboles"), "debe hacerse rollback también de la fila de 'arboles'");
                assertEquals(0, contar("miembros"));
            } finally {
                try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement()) {
                    st.execute("ALTER TABLE miembros DROP CONSTRAINT ck_prueba");
                }
            }
        }
    }

    @Nested
    @DisplayName("Etapa 4.2 - Listar")
    class Etapa2_Listar {
        @Test void sinArbolesDevuelveListaVacia() {
            assertNotNull(dao.listar());
            assertTrue(dao.listar().isEmpty());
        }

        @Test void devuelveUnResumenPorArbolOrdenadoPorId() {
            dao.guardar("Familia Doe", arbol);
            dao.guardar("Solo yo", new ArbolGenealogico(new Persona("1", "Yo", 1990, null)));
            List<ResumenArbol> lista = dao.listar();
            assertEquals(2, lista.size());
            assertEquals(1, lista.get(0).id());
            assertEquals("Familia Doe", lista.get(0).nombre());
            assertEquals(7, lista.get(0).totalMiembros());
            assertEquals(2, lista.get(1).id());
            assertEquals("Solo yo", lista.get(1).nombre());
            assertEquals(1, lista.get(1).totalMiembros());
            assertNotNull(lista.get(0).creado());
        }

        @Test void incluyeArbolesSinMiembros() throws SQLException {
            try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement()) {
                st.execute("INSERT INTO arboles (nombre) VALUES ('Vacio')");
            }
            List<ResumenArbol> lista = dao.listar();
            assertEquals(1, lista.size());
            assertEquals(0, lista.get(0).totalMiembros());
        }
    }

    @Nested
    @DisplayName("Etapa 4.3 - Cargar")
    class Etapa3_Cargar {
        @Test void reconstruyeElArbolIgualAlGuardado() {
            long id = dao.guardar("Familia Doe", arbol);
            ArbolGenealogico cargado = dao.cargar(id);
            assertNotNull(cargado);
            assertEquals(arbol.tamano(), cargado.tamano());
            assertEquals(posiciones(arbol), posiciones(cargado));
            for (Persona original : arbol.miembros()) {
                Persona p = cargado.buscar(original.getPosicion());
                assertEquals(original.getNombre(), p.getNombre());
                assertEquals(original.getNacimiento(), p.getNacimiento());
                assertEquals(original.getFallecimiento(), p.getFallecimiento());
            }
        }

        @Test void elFallecimientoNuloSigueSiendoNulo() {
            long id = dao.guardar("Familia Doe", arbol);
            ArbolGenealogico cargado = dao.cargar(id);
            assertNull(cargado.sujeto().getFallecimiento());
            assertEquals(2010, cargado.buscar("2").getFallecimiento());
        }

        @Test void cargaElArbolCorrectoCuandoHayVarios() {
            long a = dao.guardar("Uno", arbol);
            long b = dao.guardar("Dos", new ArbolGenealogico(new Persona("1", "Yo", 1990, null)));
            assertEquals(7, dao.cargar(a).tamano());
            assertEquals(1, dao.cargar(b).tamano());
            assertEquals("Yo", dao.cargar(b).sujeto().getNombre());
        }

        @Test void idInexistenteDevuelveNull() {
            assertNull(dao.cargar(99));
            dao.guardar("Familia Doe", arbol);
            assertNull(dao.cargar(0));
            assertNull(dao.cargar(-1));
        }
    }

    @Nested
    @DisplayName("Etapa 4.4 - Actualizar")
    class Etapa4_Actualizar {
        @Test void guardaLosCambiosDeUnArbol() {
            long id = dao.guardar("Familia Doe", arbol);
            ArbolGenealogico cargado = dao.cargar(id);
            cargado.agregarHijo("1", "Nuevo", 2030, null);
            cargado.eliminar("3");
            cargado.eliminar("1.1");
            assertTrue(dao.actualizar(id, cargado));

            ArbolGenealogico otra = dao.cargar(id);
            assertEquals(posiciones(cargado), posiciones(otra));
            assertEquals(cargado.tamano(), otra.tamano());
            assertEquals("Nuevo", otra.buscar("1.3").getNombre());
            assertNull(otra.buscar("1.1.1"));
        }

        @Test void noCambiaElNombreNiDuplicaFilas() {
            long id = dao.guardar("Familia Doe", arbol);
            assertTrue(dao.actualizar(id, arbol));
            assertTrue(dao.actualizar(id, arbol));
            assertEquals(1, contar("arboles"));
            assertEquals(7, contar("miembros"));
            assertEquals("Familia Doe", dao.listar().get(0).nombre());
        }

        @Test void soloAfectaAlArbolIndicado() {
            long a = dao.guardar("Uno", arbol);
            long b = dao.guardar("Dos", arbol);
            ArbolGenealogico cargado = dao.cargar(a);
            cargado.eliminar("2");
            assertTrue(dao.actualizar(a, cargado));
            assertEquals(7, dao.cargar(b).tamano());
            assertEquals(6, dao.cargar(a).tamano());
        }

        @Test void arbolInexistenteOArbolNuloDevuelveFalse() {
            assertFalse(dao.actualizar(99, arbol));
            long id = dao.guardar("Familia Doe", arbol);
            assertFalse(dao.actualizar(id, null));
            assertEquals(7, dao.cargar(id).tamano());
            assertEquals(7, contar("miembros"));
        }

        @Test void siFallaNoSePierdenLosMiembrosAnteriores() throws SQLException {
            long id = dao.guardar("Familia Doe", arbol);
            ArbolGenealogico cambiado = dao.cargar(id);
            cambiado.agregarHijo("1", "Futuro", 2050, null);
            try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement()) {
                st.execute("ALTER TABLE miembros ADD CONSTRAINT ck_prueba CHECK (nacimiento < 2050)");
            }
            try {
                assertFalse(dao.actualizar(id, cambiado));
                assertEquals(7, contar("miembros"), "debe hacerse rollback: los miembros anteriores se conservan");
                assertEquals(7, dao.cargar(id).tamano());
            } finally {
                try (Connection con = ConexionBD.conectar(); Statement st = con.createStatement()) {
                    st.execute("ALTER TABLE miembros DROP CONSTRAINT ck_prueba");
                }
            }
        }
    }

    @Nested
    @DisplayName("Etapa 4.5 - Eliminar")
    class Etapa5_Eliminar {
        @Test void eliminaElArbolYSusMiembros() {
            long id = dao.guardar("Familia Doe", arbol);
            assertTrue(dao.eliminar(id));
            assertNull(dao.cargar(id));
            assertEquals(0, contar("arboles"));
            assertEquals(0, contar("miembros"));
        }

        @Test void noAfectaAOtrosArboles() {
            long a = dao.guardar("Uno", arbol);
            long b = dao.guardar("Dos", arbol);
            assertTrue(dao.eliminar(a));
            assertEquals(1, dao.listar().size());
            assertEquals(7, dao.cargar(b).tamano());
        }

        @Test void idInexistenteDevuelveFalse() {
            assertFalse(dao.eliminar(99));
            long id = dao.guardar("Familia Doe", arbol);
            assertTrue(dao.eliminar(id));
            assertFalse(dao.eliminar(id));
        }
    }
}
