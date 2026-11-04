package co.umariana.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del ÁRBOL (ArbolGenealogico): agregar, buscar, eliminar y recorrer.
 * NO MODIFIQUE ESTE ARCHIVO: haga que pasen escribiendo código en ArbolGenealogico.java.
 *
 * Ejecutar una etapa:   ./scripts/test.sh -Dtest='ArbolGenealogicoTest$Etapa1_Sujeto'
 */
class ArbolGenealogicoTest {

    ArbolGenealogico arbol;

    @BeforeEach
    void preparar() {
        arbol = new ArbolGenealogico(new Persona("1", "John", 2000, null));
    }

    private static List<String> posiciones(List<Persona> personas) {
        return personas.stream().map(Persona::getPosicion).collect(Collectors.toList());
    }

    private void agregar(String posicion, int nacimiento) {
        arbol.agregar(new Persona(posicion, "P" + posicion, nacimiento, null));
    }

    @Nested
    @DisplayName("Etapa 2.1 - El sujeto")
    class Etapa1_Sujeto {
        @Test void unArbolNuevoSoloTieneAlSujeto() {
            assertEquals(1, arbol.tamano());
            assertEquals("John", arbol.sujeto().getNombre());
            assertEquals(List.of("1"), posiciones(arbol.miembros()));
            assertTrue(arbol.ascendientes().isEmpty());
            assertTrue(arbol.descendientes().isEmpty());
        }

        @Test void elSujetoDebeEstarEnLaPosicion1() {
            assertThrows(ArbolInvalidoException.class, () -> new ArbolGenealogico(new Persona("2", "Henry", 1920, null)));
            assertThrows(ArbolInvalidoException.class, () -> new ArbolGenealogico(new Persona("1.1", "Timmy", 2021, null)));
        }

        @Test void elSujetoDebeSerUnaPersonaValida() {
            assertThrows(IllegalArgumentException.class, () -> new ArbolGenealogico(new Persona("1", "", 2000, null)));
            assertThrows(IllegalArgumentException.class, () -> new ArbolGenealogico(new Persona("1", "John", 2000, 1990)));
        }

        @Test void buscarYContener() {
            assertEquals("John", arbol.buscar("1").getNombre());
            assertTrue(arbol.contiene("1"));
            assertNull(arbol.buscar("2"));
            assertFalse(arbol.contiene("1.1"));
            assertNull(arbol.buscar("xyz"));
            assertNull(arbol.buscar(null));
            assertFalse(arbol.contiene("0"));
        }
    }

    @Nested
    @DisplayName("Etapa 2.2 - Agregar ascendientes (Ahnentafel)")
    class Etapa2_Ascendientes {
        @Test void agregaPadreYMadre() {
            agregar("2", 1970);
            agregar("3", 1972);
            assertEquals(3, arbol.tamano());
            assertEquals("P2", arbol.buscar("2").getNombre());
            assertEquals("P3", arbol.buscar("3").getNombre());
        }

        @Test void encuentraAbuelosYBisabuelos() {
            agregar("2", 1970);
            agregar("5", 1945);
            agregar("10", 1920);
            agregar("11", 1921);
            assertEquals("P10", arbol.buscar("10").getNombre());
            assertEquals("P11", arbol.buscar("11").getNombre());
            assertNull(arbol.buscar("4"));
            assertNull(arbol.buscar("9"));
            assertFalse(arbol.contiene("20"));
        }

        @Test void noSePuedeAgregarSinSuHijo() {
            assertThrows(ArbolInvalidoException.class, () -> agregar("4", 1940));
            assertThrows(ArbolInvalidoException.class, () -> agregar("5", 1940));
        }

        @Test void debeHaberNacidoAntesQueSuHijo() {
            assertThrows(ArbolInvalidoException.class, () -> agregar("2", 2000));
            assertThrows(ArbolInvalidoException.class, () -> agregar("2", 2010));
            agregar("2", 1999);
            assertThrows(ArbolInvalidoException.class, () -> agregar("4", 1999));
        }

        @Test void noSePuedeRepetirUnaPosicion() {
            agregar("2", 1970);
            assertThrows(ArbolInvalidoException.class, () -> agregar("2", 1960));
            assertThrows(ArbolInvalidoException.class, () -> agregar("1", 1960));
            assertEquals(2, arbol.tamano());
        }

        @Test void personaInvalidaSeRechazaConIllegalArgument() {
            assertThrows(IllegalArgumentException.class, () -> arbol.agregar(new Persona("2", "", 1970, null)));
            assertThrows(IllegalArgumentException.class, () -> arbol.agregar(new Persona("2", "X", 1970, 1960)));
            assertThrows(IllegalArgumentException.class, () -> arbol.agregar(new Persona("02", "X", 1970, null)));
            assertEquals(1, arbol.tamano());
        }

        @Test void agregarDevuelveLaPersona() {
            Persona p = new Persona("2", "Henry", 1970, null);
            assertSame(p, arbol.agregar(p));
        }

        @Test void agregarPadreYMadreCalculanLaPosicion() {
            Persona padre = arbol.agregarPadre("1", "Henry", 1970, 2000);
            Persona madre = arbol.agregarMadre("1", "Maria", 1972, null);
            assertEquals("2", padre.getPosicion());
            assertEquals("3", madre.getPosicion());
            Persona abuelo = arbol.agregarPadre("3", "Luis", 1940, null);
            assertEquals("6", abuelo.getPosicion());
            Persona abuela = arbol.agregarMadre("3", "Rosa", 1942, null);
            assertEquals("7", abuela.getPosicion());
            assertEquals(5, arbol.tamano());
        }

        @Test void agregarPadreRechazaCasosInvalidos() {
            arbol.agregarPadre("1", "Henry", 1970, null);
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarPadre("1", "Otro", 1971, null));   // ya existe
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarPadre("9", "X", 1900, null));      // no existe
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarMadre("4", "X", 1900, null));      // no existe
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarPadre("1", "X", 2005, null) );     // ya existe (2) y/o nacería después
            assertEquals(2, arbol.tamano());
        }

        @Test void noSeAgreganPadresADescendientes() {
            arbol.agregarHijo("1", "Timmy", 2021, null);
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarPadre("1.1", "X", 1990, null));
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarMadre("1.1", "X", 1990, null));
        }

        @Test void agregarPadreConDatosInvalidos() {
            assertThrows(IllegalArgumentException.class, () -> arbol.agregarPadre("1", "", 1970, null));
            assertThrows(IllegalArgumentException.class, () -> arbol.agregarPadre("1", "Henry", 1970, 1960));
            assertFalse(arbol.contiene("2"));
        }
    }

    @Nested
    @DisplayName("Etapa 2.3 - Agregar descendientes (d'Aboville)")
    class Etapa3_Descendientes {
        @Test void agregaHijosYNietos() {
            agregar("1.1", 2021);
            agregar("1.2", 2026);
            agregar("1.1.1", 2040);
            assertEquals(4, arbol.tamano());
            assertEquals("P1.1.1", arbol.buscar("1.1.1").getNombre());
            assertNull(arbol.buscar("1.2.1"));
            assertNull(arbol.buscar("1.3"));
        }

        @Test void puedeFaltarUnHermano() {
            agregar("1.2", 2010);       // no existe el 1.1
            agregar("1.2.5", 2030);     // ni el 1.2.1 ... 1.2.4
            assertEquals("P1.2.5", arbol.buscar("1.2.5").getNombre());
            assertNull(arbol.buscar("1.1"));
            assertNull(arbol.buscar("1.2.1"));
        }

        @Test void noSePuedeAgregarSinSuProgenitor() {
            assertThrows(ArbolInvalidoException.class, () -> agregar("1.1.1", 2040));
            assertThrows(ArbolInvalidoException.class, () -> agregar("1.2.1", 2040));
        }

        @Test void debeHaberNacidoDespuesQueSuProgenitor() {
            assertThrows(ArbolInvalidoException.class, () -> agregar("1.1", 2000));
            assertThrows(ArbolInvalidoException.class, () -> agregar("1.1", 1990));
            agregar("1.1", 2001);
            assertThrows(ArbolInvalidoException.class, () -> agregar("1.1.1", 2001));
        }

        @Test void noSePuedeRepetirUnaPosicion() {
            agregar("1.1", 2021);
            assertThrows(ArbolInvalidoException.class, () -> agregar("1.1", 2022));
            assertEquals(2, arbol.tamano());
        }

        @Test void agregarHijoUsaElSiguienteNumero() {
            assertEquals("1.1", arbol.agregarHijo("1", "A", 2021, null).getPosicion());
            assertEquals("1.2", arbol.agregarHijo("1", "B", 2022, null).getPosicion());
            assertEquals("1.1.1", arbol.agregarHijo("1.1", "C", 2040, null).getPosicion());
            assertEquals(4, arbol.tamano());
        }

        @Test void agregarHijoUsaElMayorNumeroExistenteMasUno() {
            agregar("1.1", 2021);
            agregar("1.3", 2023);
            assertEquals("1.4", arbol.agregarHijo("1", "D", 2024, null).getPosicion());
        }

        @Test void agregarHijoRechazaCasosInvalidos() {
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarHijo("1.9", "X", 2020, null));    // no existe
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarHijo("5", "X", 2020, null));      // no existe
            arbol.agregarPadre("1", "Henry", 1970, null);
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarHijo("2", "X", 2020, null));      // es ascendiente
            assertThrows(ArbolInvalidoException.class, () -> arbol.agregarHijo("1", "X", 1990, null));      // nace antes que el sujeto
            assertThrows(IllegalArgumentException.class, () -> arbol.agregarHijo("1", "", 2020, null));
            assertEquals(2, arbol.tamano());
        }
    }

    @Nested
    @DisplayName("Etapa 2.4 - Eliminar")
    class Etapa4_Eliminar {
        @BeforeEach
        void arbolGrande() {
            agregar("2", 1970);
            agregar("3", 1972);
            agregar("4", 1940);
            agregar("5", 1942);
            agregar("8", 1910);
            agregar("1.1", 2021);
            agregar("1.2", 2023);
            agregar("1.1.1", 2040);
            agregar("1.1.2", 2041);
        }

        @Test void elSujetoNoSePuedeEliminar() {
            assertThrows(ArbolInvalidoException.class, () -> arbol.eliminar("1"));
            assertEquals(10, arbol.tamano());
        }

        @Test void noSePuedeEliminarLoQueNoExiste() {
            assertThrows(ArbolInvalidoException.class, () -> arbol.eliminar("9"));
            assertThrows(ArbolInvalidoException.class, () -> arbol.eliminar("1.3"));
            assertThrows(ArbolInvalidoException.class, () -> arbol.eliminar("xyz"));
            assertThrows(ArbolInvalidoException.class, () -> arbol.eliminar(null));
            assertEquals(10, arbol.tamano());
        }

        @Test void eliminarUnPadreEliminaSusAscendientes() {
            assertEquals(4, arbol.eliminar("2"));   // 2, 4, 5 y 8
            assertEquals(6, arbol.tamano());
            for (String p : new String[]{"2", "4", "5", "8"}) {
                assertFalse(arbol.contiene(p), p);
            }
            assertTrue(arbol.contiene("3"));
            assertEquals(List.of("1", "3", "1.1", "1.1.1", "1.1.2", "1.2"), posiciones(arbol.miembros()));
        }

        @Test void eliminarUnaHojaDeAscendientes() {
            assertEquals(1, arbol.eliminar("8"));
            assertEquals(9, arbol.tamano());
            assertTrue(arbol.contiene("4"));
        }

        @Test void eliminarUnHijoEliminaSusDescendientes() {
            assertEquals(3, arbol.eliminar("1.1"));  // 1.1, 1.1.1 y 1.1.2
            assertEquals(7, arbol.tamano());
            assertFalse(arbol.contiene("1.1"));
            assertFalse(arbol.contiene("1.1.1"));
            assertTrue(arbol.contiene("1.2"));
        }

        @Test void lasDemasPosicionesNoCambian() {
            arbol.eliminar("1.1");
            assertEquals("P1.2", arbol.buscar("1.2").getNombre());
            assertEquals("1.3", arbol.agregarHijo("1", "Nuevo", 2030, null).getPosicion());  // el siguiente al mayor (1.2)
        }

        @Test void despuesDeEliminarSePuedeVolverAAgregar() {
            arbol.eliminar("2");
            assertEquals("2", arbol.agregarPadre("1", "Otro", 1969, null).getPosicion());
            assertFalse(arbol.contiene("4"));
        }
    }

    @Nested
    @DisplayName("Etapa 2.5 - Recorridos")
    class Etapa5_Recorridos {
        @BeforeEach
        void arbolGrande() {
            // se agregan mezclados a propósito (cada uno después de la persona de la que depende)
            agregar("3", 1972);
            agregar("1.2", 2023);
            agregar("7", 1945);
            agregar("1.1", 2021);
            agregar("2", 1970);
            agregar("1.1.2", 2041);
            agregar("4", 1940);
            agregar("1.1.1", 2040);
        }

        @Test void miembrosEnOrden() {
            assertEquals(List.of("1", "2", "3", "4", "7", "1.1", "1.1.1", "1.1.2", "1.2"), posiciones(arbol.miembros()));
        }

        @Test void ascendientesPorNumeroAhnentafel() {
            assertEquals(List.of("2", "3", "4", "7"), posiciones(arbol.ascendientes()));
        }

        @Test void descendientesEnPreorden() {
            assertEquals(List.of("1.1", "1.1.1", "1.1.2", "1.2"), posiciones(arbol.descendientes()));
        }

        @Test void lasListasSonCopias() {
            arbol.miembros().clear();
            arbol.ascendientes().clear();
            arbol.descendientes().clear();
            assertEquals(9, arbol.tamano());
            assertEquals(9, arbol.miembros().size());
        }

        @Test void tamanoCoincideConLosMiembros() {
            assertEquals(arbol.miembros().size(), arbol.tamano());
            arbol.eliminar("1.1");
            assertEquals(arbol.miembros().size(), arbol.tamano());
            arbol.eliminar("3");
            assertEquals(arbol.miembros().size(), arbol.tamano());
        }

        @Test void cadaPersonaApareceDespuesDeLaQueNecesita() {
            List<Persona> lista = arbol.miembros();
            ArbolGenealogico copia = new ArbolGenealogico(lista.get(0));
            for (Persona p : lista.subList(1, lista.size())) {
                copia.agregar(p);   // si el orden fuera incorrecto lanzaría ArbolInvalidoException
            }
            assertEquals(lista.size(), copia.tamano());
        }
    }

    @Nested
    @DisplayName("Etapa 2.6 - Estructura propia")
    class Etapa6_Estructura {
        @Test void noUsaMapNiSet() throws IOException {
            String fuente = Files.readString(Path.of("src/main/java/co/umariana/modelo/ArbolGenealogico.java"));
            fuente = fuente.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", "");
            assertFalse(fuente.matches("(?s).*\\b(Map|HashMap|TreeMap|LinkedHashMap|Set|HashSet|TreeSet|LinkedHashSet)\\b.*"),
                    "ArbolGenealogico debe usar sus propios nodos: no se permite Map ni Set");
            assertTrue(fuente.contains("Nodo"), "ArbolGenealogico debe declarar su propia clase Nodo");
        }

        @Test void arbolGrandeConDiezGeneraciones() {
            // cadena de padres: 2, 4, 8, ... 1024 (cada uno nace 10 años antes que su hijo)
            for (int n = 2; n <= 1024; n *= 2) {
                arbol.agregar(new Persona(String.valueOf(n), "A" + n, 2000 - 10 * Posicion.generacion(String.valueOf(n)), null));
            }
            assertEquals(11, arbol.tamano());
            assertTrue(arbol.contiene("1024"));
            assertEquals("A1024", arbol.buscar("1024").getNombre());
            assertNull(arbol.buscar("1025"));
        }
    }
}
