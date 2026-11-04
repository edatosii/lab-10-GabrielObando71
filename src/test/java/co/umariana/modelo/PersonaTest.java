package co.umariana.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los MODELOS (2/2): las validaciones de una Persona (por ejemplo, que no muera antes de nacer).
 * NO MODIFIQUE ESTE ARCHIVO: haga que pasen escribiendo código en Persona.java.
 *
 * Ejecutar una etapa:   ./scripts/test.sh -Dtest='PersonaTest$Etapa1_Validar'
 */
class PersonaTest {

    private static void invalida(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        assertThrows(IllegalArgumentException.class, () -> new Persona(posicion, nombre, nacimiento, fallecimiento).validar());
    }

    private static void valida(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        assertDoesNotThrow(() -> new Persona(posicion, nombre, nacimiento, fallecimiento).validar());
    }

    @Nested
    @DisplayName("Etapa 1.3 - Validar una persona")
    class Etapa1_Validar {
        @Test void personaCorrecta() {
            valida("1", "John Doe", 2002, null);
            valida("2", "Henry Doe", 1920, 1990);
            valida("1.2.1", "Amy Doe", 2026, null);
        }

        @Test void posicionInvalida() {
            invalida(null, "John", 2000, null);
            invalida("", "John", 2000, null);
            invalida("0", "John", 2000, null);
            invalida("2.1", "John", 2000, null);
            invalida("1.", "John", 2000, null);
            invalida("abc", "John", 2000, null);
        }

        @Test void nombreObligatorio() {
            invalida("1", null, 2000, null);
            invalida("1", "", 2000, null);
            invalida("1", "   ", 2000, null);
        }

        @Test void nombreConLongitudMaxima() {
            valida("1", "a".repeat(Persona.MAX_NOMBRE), 2000, null);
            invalida("1", "a".repeat(Persona.MAX_NOMBRE + 1), 2000, null);
        }

        @Test void nombreSinPuntoYComaNiSaltosDeLinea() {
            invalida("1", "Doe; John", 2000, null);
            invalida("1", "John\nDoe", 2000, null);
            invalida("1", "John\rDoe", 2000, null);
        }

        @Test void nacimientoDebeSerPositivo() {
            valida("1", "John", 1, null);
            invalida("1", "John", 0, null);
            invalida("1", "John", -50, null);
        }

        @Test void fallecimientoNoPuedeSerAnteriorAlNacimiento() {
            invalida("1", "John", 2000, 1999);
            invalida("1", "John", 2000, 1);
        }

        @Test void fallecimientoIgualAlNacimientoEsValido() {
            valida("1", "John", 2000, 2000);
            valida("1", "John", 2000, 2001);
        }

        @Test void elMensajeExplicaElProblema() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> new Persona("1", "John", 2000, 1990).validar());
            assertNotNull(e.getMessage());
            assertFalse(e.getMessage().isBlank());
        }
    }

    @Nested
    @DisplayName("Datos de la persona")
    class Datos {
        @Test void guardaSusDatos() {
            Persona p = new Persona("1.1", "Timmy", 2021, null);
            assertEquals("1.1", p.getPosicion());
            assertEquals("Timmy", p.getNombre());
            assertEquals(2021, p.getNacimiento());
            assertNull(p.getFallecimiento());
        }
    }
}
