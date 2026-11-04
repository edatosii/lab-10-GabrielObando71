package co.umariana.construccion;

import static org.junit.jupiter.api.Assertions.*;

import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ArbolInvalidoException;
import co.umariana.modelo.Persona;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la CREACIÓN del árbol a partir del texto (ConstructorArbol).
 * NO MODIFIQUE ESTE ARCHIVO: haga que pasen escribiendo código en ConstructorArbol.java.
 *
 * Ejecutar una etapa:   ./scripts/test.sh -Dtest='ConstructorArbolTest$Etapa1_TextoValido'
 */
class ConstructorArbolTest {

    private static List<String> posiciones(List<Persona> personas) {
        return personas.stream().map(Persona::getPosicion).collect(Collectors.toList());
    }

    private static ArbolInvalidoException error(String texto) {
        return assertThrows(ArbolInvalidoException.class, () -> ConstructorArbol.construir(texto));
    }

    @Nested
    @DisplayName("Etapa 3.1 - Texto válido")
    class Etapa1_TextoValido {
        @Test void soloElSujeto() {
            ArbolGenealogico a = ConstructorArbol.construir("1;John Doe;2002;");
            assertEquals(1, a.tamano());
            assertEquals("John Doe", a.sujeto().getNombre());
            assertEquals(2002, a.sujeto().getNacimiento());
            assertNull(a.sujeto().getFallecimiento());
        }

        @Test void ejemploCompleto() {
            String texto = "1;John Doe;2002;\n"
                    + "2;Henry Doe;1920;1990\n"
                    + "3;Maria Doe;1921;\n"
                    + "1.1;Timmy Doe;2021;\n"
                    + "1.2;Amy Doe;2026;\n";
            ArbolGenealogico a = ConstructorArbol.construir(texto);
            assertEquals(5, a.tamano());
            assertEquals(1990, a.buscar("2").getFallecimiento());
            assertEquals("Amy Doe", a.buscar("1.2").getNombre());
            assertEquals(List.of("1", "2", "3", "1.1", "1.2"), posiciones(a.miembros()));
        }

        @Test void aceptaFinesDeLineaDeWindows() {
            ArbolGenealogico a = ConstructorArbol.construir("1;John;2002;\r\n2;Henry;1970;1999\r\n1.1;Tim;2025;\r\n");
            assertEquals(3, a.tamano());
            assertEquals(1999, a.buscar("2").getFallecimiento());
        }

        @Test void ignoraLineasEnBlancoYRecortaEspacios() {
            ArbolGenealogico a = ConstructorArbol.construir("\n  1 ; John Doe ; 2002 ; \n\n   \n 2;Henry;1970; 1999 \n\n");
            assertEquals(2, a.tamano());
            assertEquals("John Doe", a.sujeto().getNombre());
            assertEquals(1999, a.buscar("2").getFallecimiento());
        }

        @Test void aceptaLineasConTresCampos() {
            ArbolGenealogico a = ConstructorArbol.construir("1;John;2002\n1.1;Tim;2025");
            assertEquals(2, a.tamano());
            assertNull(a.buscar("1.1").getFallecimiento());
        }

        @Test void aceptaLasLineasEnCualquierOrden() {
            String texto = "1.1.1;Nieto;2040;\n"
                    + "7;Bisabuela;1945;\n"
                    + "1.2;Amy;2026;\n"
                    + "3;Maria;1972;\n"
                    + "1;John;2000;\n"
                    + "1.1;Timmy;2021;\n"
                    + "2;Henry;1970;\n"
                    + "4;Abuelo;1940;\n";
            ArbolGenealogico a = ConstructorArbol.construir(texto);
            assertEquals(8, a.tamano());
            assertEquals(List.of("1", "2", "3", "4", "7", "1.1", "1.1.1", "1.2"), posiciones(a.miembros()));
        }

        @Test void soloMuestraLosMiembrosDelTexto() {
            // faltan el 3 y el 1.1: no se crean personas vacías
            ArbolGenealogico a = ConstructorArbol.construir("1;John;2000;\n2;Henry;1970;\n1.2;Amy;2026;");
            assertEquals(3, a.tamano());
            assertNull(a.buscar("3"));
            assertNull(a.buscar("1.1"));
            assertEquals(List.of("1", "2", "1.2"), posiciones(a.miembros()));
        }
    }

    @Nested
    @DisplayName("Etapa 3.2 - Texto vacío o sin sujeto")
    class Etapa2_TextoIncompleto {
        @Test void textoNuloOVacio() {
            assertTrue(error(null).getMessage().contains("vacío"));
            assertTrue(error("").getMessage().contains("vacío"));
            assertTrue(error("   \n \r\n\t\n").getMessage().contains("vacío"));
        }

        @Test void faltaElSujeto() {
            ArbolInvalidoException e = error("2;Henry;1970;\n3;Maria;1972;");
            assertFalse(e.getMessage().startsWith("Línea"), e.getMessage());
            assertTrue(e.getMessage().contains("1"), e.getMessage());
        }
    }

    @Nested
    @DisplayName("Etapa 3.3 - Líneas mal formadas (el mensaje empieza con el número de línea)")
    class Etapa3_LineasInvalidas {
        @Test void camposDeMenosOMasDelEsperado() {
            assertTrue(error("1;John;2000;\n2;Henry").getMessage().startsWith("Línea 2: "));
            assertTrue(error("1;John").getMessage().startsWith("Línea 1: "));
            assertTrue(error("1;John;2000;;").getMessage().startsWith("Línea 1: "));
            assertTrue(error("1;John;2000;2010;extra").getMessage().startsWith("Línea 1: "));
        }

        @Test void anioQueNoEsNumero() {
            assertTrue(error("1;John;dos mil;").getMessage().startsWith("Línea 1: "));
            assertTrue(error("1;John;2000;\n2;Henry;1970;tarde").getMessage().startsWith("Línea 2: "));
            assertTrue(error("1;John;20.5;").getMessage().startsWith("Línea 1: "));
            assertTrue(error("1;John;;").getMessage().startsWith("Línea 1: "));
        }

        @Test void posicionInvalida() {
            assertTrue(error("1;John;2000;\n2.1;Raro;1970;").getMessage().startsWith("Línea 2: "));
            assertTrue(error("1;John;2000;\n0;Cero;1970;").getMessage().startsWith("Línea 2: "));
            assertTrue(error("1;John;2000;\nabc;Letras;1970;").getMessage().startsWith("Línea 2: "));
            assertTrue(error("1;John;2000;\n1.;Punto;2020;").getMessage().startsWith("Línea 2: "));
        }

        @Test void nombreVacio() {
            assertTrue(error("1;;2000;").getMessage().startsWith("Línea 1: "));
            assertTrue(error("1;John;2000;\n2;   ;1970;").getMessage().startsWith("Línea 2: "));
        }

        @Test void fallecimientoAnteriorAlNacimiento() {
            assertTrue(error("1;John;2000;\n2;Henry;1970;1960").getMessage().startsWith("Línea 2: "));
        }

        @Test void nacimientoNoPositivo() {
            assertTrue(error("1;John;0;").getMessage().startsWith("Línea 1: "));
        }

        @Test void elNumeroDeLineaCuentaLasLineasEnBlanco() {
            // la línea con el error es la 4 del texto (aunque la 2 y la 3 estén en blanco)
            assertTrue(error("1;John;2000;\n\n   \n2;Henry;abc;").getMessage().startsWith("Línea 4: "));
            assertTrue(error("\n\n1;John;2000;\r\n\r\n2;Henry").getMessage().startsWith("Línea 5: "));
        }
    }

    @Nested
    @DisplayName("Etapa 3.4 - Reglas del árbol")
    class Etapa4_ReglasDelArbol {
        @Test void posicionRepetida() {
            ArbolInvalidoException e = error("1;John;2000;\n2;Henry;1970;\n3;Maria;1972;\n2;Otro;1960;");
            assertTrue(e.getMessage().startsWith("Línea 4: "), e.getMessage());
        }

        @Test void sujetoRepetido() {
            assertTrue(error("1;John;2000;\n1;John;2001;").getMessage().startsWith("Línea 2: "));
        }

        @Test void ascendienteSinSuHijo() {
            // el 5 necesita al 2, que no está
            ArbolInvalidoException e = error("1;John;2000;\n5;Tia;1940;");
            assertTrue(e.getMessage().startsWith("Línea 2: "), e.getMessage());
        }

        @Test void descendienteSinSuProgenitor() {
            ArbolInvalidoException e = error("1;John;2000;\n1.1.1;Nieto;2040;");
            assertTrue(e.getMessage().startsWith("Línea 2: "), e.getMessage());
        }

        @Test void ascendienteNacidoDespuesDeSuHijo() {
            ArbolInvalidoException e = error("1;John;2000;\n2;Henry;2005;");
            assertTrue(e.getMessage().startsWith("Línea 2: "), e.getMessage());
            assertTrue(error("1;John;2000;\n2;Henry;2000;").getMessage().startsWith("Línea 2: "));
        }

        @Test void descendienteNacidoAntesQueSuProgenitor() {
            ArbolInvalidoException e = error("1;John;2000;\n1.1;Tim;1999;");
            assertTrue(e.getMessage().startsWith("Línea 2: "), e.getMessage());
            assertTrue(error("1;John;2000;\n1.1;Tim;2000;").getMessage().startsWith("Línea 2: "));
        }

        @Test void elErrorSeInformaEnLaLineaCorrectaAunqueLasLineasVenganDesordenadas() {
            // la línea 1 (nieto) es válida; el problema está en la línea 3 (el hijo 1.1 nace antes que el sujeto)
            ArbolInvalidoException e = error("1.1.1;Nieto;2040;\n1;John;2000;\n1.1;Tim;1990;");
            assertTrue(e.getMessage().startsWith("Línea 3: "), e.getMessage());
        }

        @Test void siElSujetoEstaAlFinalNoCambiaElResultado() {
            ArbolGenealogico a = ConstructorArbol.construir("1.1;Tim;2021;\n2;Henry;1970;\n1;John;2000;");
            assertEquals(3, a.tamano());
        }
    }
}
