package co.umariana.modelo;

/**
 * Un miembro del árbol genealógico: su posición, su nombre y sus años de nacimiento y fallecimiento
 * (el fallecimiento es opcional: null si la persona sigue viva o no se sabe).
 *
 * La clase ya tiene sus atributos, constructor y getters. Lo que falta es {@link #validar()} (Etapa 1).
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 1)
 * ------------------------------------------
 * Reemplace el "throw new UnsupportedOperationException" de validar() por su implementación.
 * NO modifique las pruebas (src/test).
 */
public class Persona {

    /** Longitud máxima del nombre (es lo que cabe en la base de datos). */
    public static final int MAX_NOMBRE = 100;

    private final String posicion;
    private final String nombre;
    private final int nacimiento;
    private final Integer fallecimiento;

    public Persona(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        this.posicion = posicion;
        this.nombre = nombre;
        this.nacimiento = nacimiento;
        this.fallecimiento = fallecimiento;
    }

    /**
     * Revisa que la persona sea válida. Si no lo es, lanza {@link IllegalArgumentException} con un mensaje
     * que explique el problema. Reglas (se revisan en este orden):
     * <ul>
     *   <li>La posición debe ser válida (Posicion.esValida): Ahnentafel o d'Aboville.</li>
     *   <li>El nombre no puede ser nulo ni estar en blanco, ni tener más de MAX_NOMBRE caracteres.</li>
     *   <li>El nombre no puede contener el carácter {@code ;} (separa los campos del texto) ni saltos de línea.</li>
     *   <li>El año de nacimiento debe ser mayor que 0.</li>
     *   <li>El año de fallecimiento es opcional (null), pero si existe NO puede ser menor que el de nacimiento
     *       (si es igual, es válido: la persona murió el mismo año en que nació).</li>
     * </ul>
     */
    public void validar() {
        // TODO Etapa 1
        throw new UnsupportedOperationException("TODO: validar");
    }

    public String getPosicion() { return posicion; }
    public String getNombre() { return nombre; }
    public int getNacimiento() { return nacimiento; }
    public Integer getFallecimiento() { return fallecimiento; }

    @Override
    public String toString() {
        return posicion + " - " + nombre + " (" + nacimiento + "-" + (fallecimiento == null ? "" : fallecimiento) + ")";
    }
}
