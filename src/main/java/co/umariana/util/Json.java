package co.umariana.util;

import co.umariana.modelo.Persona;
import java.util.List;

/**
 * Convierte los miembros de un árbol a JSON para que el canvas (js/arbol.js) los dibuje.
 * El resultado es seguro para escribirlo dentro de un {@code <script>} de una página.
 *
 * Esta clase YA está completa: no es necesario modificarla.
 */
public final class Json {

    private Json() {
    }

    /** [{"posicion":"1","nombre":"Ana","nacimiento":2000,"fallecimiento":null}, ...] */
    public static String miembros(List<Persona> miembros) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < miembros.size(); i++) {
            Persona p = miembros.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"posicion\":").append(texto(p.getPosicion()))
              .append(",\"nombre\":").append(texto(p.getNombre()))
              .append(",\"nacimiento\":").append(p.getNacimiento())
              .append(",\"fallecimiento\":").append(p.getFallecimiento() == null ? "null" : p.getFallecimiento())
              .append('}');
        }
        return sb.append(']').toString();
    }

    private static String texto(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : (valor == null ? "" : valor).toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");   // evita cerrar el <script> con "</script>"
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
