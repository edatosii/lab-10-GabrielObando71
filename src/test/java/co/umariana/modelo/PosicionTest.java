package co.umariana.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los MODELOS (1/2): las posiciones Ahnentafel y d'Aboville.
 * NO MODIFIQUE ESTE ARCHIVO: haga que pasen escribiendo código en Posicion.java.
 *
 * Ejecutar una etapa:   ./scripts/test.sh -Dtest='PosicionTest$Etapa1_Clasificar'
 */
class PosicionTest {

    @Nested
    @DisplayName("Etapa 1.1 - Clasificar posiciones")
    class Etapa1_Clasificar {
        @Test void sujeto() {
            assertTrue(Posicion.esSujeto("1"));
            assertFalse(Posicion.esSujeto("2"));
            assertFalse(Posicion.esSujeto("1.1"));
            assertFalse(Posicion.esSujeto(null));
        }

        @Test void ahnentafelValidas() {
            for (String p : new String[]{"1", "2", "3", "13", "1024", "2047"}) {
                assertTrue(Posicion.esAhnentafel(p), p);
            }
        }

        @Test void ahnentafelInvalidas() {
            for (String p : new String[]{null, "", "0", "01", "-2", "2.0", " 2", "2 ", "2048", "abc", "1.1", "10000"}) {
                assertFalse(Posicion.esAhnentafel(p), String.valueOf(p));
            }
        }

        @Test void dabovilleValidas() {
            for (String p : new String[]{"1.1", "1.2.3", "1.9999", "1.1.1.1.1"}) {
                assertTrue(Posicion.esDAboville(p), p);
            }
        }

        @Test void dabovilleInvalidas() {
            for (String p : new String[]{null, "", "1", "2.1", "1.", "1..1", "1.0", "1.01", "1.10000", ".1", "1.1.", "1.a", "1.-1", "1. 1"}) {
                assertFalse(Posicion.esDAboville(p), String.valueOf(p));
            }
        }

        @Test void dabovilleNoPuedePasarDe50Caracteres() {
            assertTrue(Posicion.esDAboville("1" + ".1".repeat(24)));    // 49 caracteres
            assertFalse(Posicion.esDAboville("1" + ".1".repeat(25)));   // 51 caracteres
        }

        @Test void validas() {
            assertTrue(Posicion.esValida("1"));
            assertTrue(Posicion.esValida("7"));
            assertTrue(Posicion.esValida("1.2"));
            assertFalse(Posicion.esValida("2.1"));
            assertFalse(Posicion.esValida(null));
            assertFalse(Posicion.esValida("0"));
        }

        @Test void ascendienteODescendiente() {
            assertFalse(Posicion.esAscendiente("1"));
            assertTrue(Posicion.esAscendiente("2"));
            assertTrue(Posicion.esAscendiente("15"));
            assertFalse(Posicion.esAscendiente("1.1"));
            assertFalse(Posicion.esAscendiente(null));
            assertFalse(Posicion.esDescendiente("1"));
            assertTrue(Posicion.esDescendiente("1.1"));
            assertTrue(Posicion.esDescendiente("1.3.2"));
            assertFalse(Posicion.esDescendiente("5"));
        }
    }

    @Nested
    @DisplayName("Etapa 1.2 - Navegar entre posiciones")
    class Etapa2_Navegar {
        @Test void padreYMadre() {
            assertEquals("2", Posicion.posicionDelPadre("1"));
            assertEquals("3", Posicion.posicionDeLaMadre("1"));
            assertEquals("10", Posicion.posicionDelPadre("5"));
            assertEquals("11", Posicion.posicionDeLaMadre("5"));
        }

        @Test void padreYMadreRechazanPosicionesInvalidas() {
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDelPadre("1.1"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDeLaMadre("abc"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDelPadre(null));
        }

        @Test void hijoEnLinea() {
            assertEquals("1", Posicion.posicionDelHijoEnLinea("2"));
            assertEquals("1", Posicion.posicionDelHijoEnLinea("3"));
            assertEquals("2", Posicion.posicionDelHijoEnLinea("5"));
            assertEquals("5", Posicion.posicionDelHijoEnLinea("11"));
        }

        @Test void hijoEnLineaRechazaAlSujetoYALosDescendientes() {
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDelHijoEnLinea("1"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDelHijoEnLinea("1.2"));
        }

        @Test void progenitor() {
            assertEquals("1", Posicion.posicionDelProgenitor("1.2"));
            assertEquals("1.2", Posicion.posicionDelProgenitor("1.2.3"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDelProgenitor("1"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.posicionDelProgenitor("4"));
        }

        @Test void orden() {
            assertEquals(2, Posicion.orden("1.2"));
            assertEquals(3, Posicion.orden("1.2.3"));
            assertEquals(12, Posicion.orden("1.12"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.orden("1"));
        }

        @Test void generacion() {
            assertEquals(0, Posicion.generacion("1"));
            assertEquals(1, Posicion.generacion("2"));
            assertEquals(1, Posicion.generacion("3"));
            assertEquals(2, Posicion.generacion("4"));
            assertEquals(2, Posicion.generacion("7"));
            assertEquals(3, Posicion.generacion("8"));
            assertEquals(3, Posicion.generacion("15"));
            assertEquals(10, Posicion.generacion("2047"));
            assertEquals(1, Posicion.generacion("1.1"));
            assertEquals(2, Posicion.generacion("1.2.3"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.generacion("2.1"));
            assertThrows(IllegalArgumentException.class, () -> Posicion.generacion(null));
        }
    }
}
