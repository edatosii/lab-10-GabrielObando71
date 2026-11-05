package co.umariana.construccion;

import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ArbolInvalidoException;
import co.umariana.modelo.Persona;
import co.umariana.modelo.Posicion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConstructorArbol {

    private ConstructorArbol() {
    }

    /**
     * Convierte el texto en un árbol (ver las reglas arriba).
     *
     * @throws ArbolInvalidoException si el texto no cumple las reglas
     */
    public static ArbolGenealogico construir(String texto) {
        if (texto == null) {
            throw new ArbolInvalidoException("El texto está vacío.");
        }

        String[] lineas = texto.split("\\r?\\n");
        if (lineas.length == 0) {
            throw new ArbolInvalidoException("El texto está vacío.");
        }

        class RegistroPersona {
            int numeroLinea;
            String posicion;
            String nombre;
            int nacimiento;
            Integer fallecimiento;
            Persona persona;

            RegistroPersona(int numeroLinea, String posicion, String nombre, int nacimiento, Integer fallecimiento) {
                this.numeroLinea = numeroLinea;
                this.posicion = posicion;
                this.nombre = nombre;
                this.nacimiento = nacimiento;
                this.fallecimiento = fallecimiento;
                this.persona = new Persona(posicion, nombre, nacimiento, fallecimiento);
            }
        }

        List<RegistroPersona> registros = new ArrayList<>();
        Set<String> posicionesVistas = new HashSet<>();
        boolean tieneSujeto = false;
        boolean tieneContenidoReal = false;

        for (int i = 0; i < lineas.length; i++) {
            int numLinea = i + 1;
            String linea = lineas[i];
            
            // Las líneas en blanco se ignoran, pero el número de línea cuenta todas
            if (linea.trim().isEmpty()) {
                continue;
            }
            tieneContenidoReal = true;

            // Se aceptan 3 o 4 campos separados por ';'
            String[] partes = linea.split(";", -1);
            if (partes.length < 3 || partes.length > 4) {
                throw new ArbolInvalidoException("Línea " + numLinea + ": la cantidad de campos debe ser 3 o 4.");
            }

            String posicion = partes[0].trim();
            String nombre = partes[1].trim();
            String strNacimiento = partes[2].trim();
            String strFallecimiento = partes.length == 4 ? partes[3].trim() : "";

            if (!Posicion.esValida(posicion)) {
                throw new ArbolInvalidoException("Línea " + numLinea + ": la posición no es válida.");
            }

            if (posicionesVistas.contains(posicion)) {
                throw new ArbolInvalidoException("Línea " + numLinea + ": la posición " + posicion + " está repetida.");
            }

            int nacimiento;
            try {
                nacimiento = Integer.parseInt(strNacimiento);
            } catch (NumberFormatException e) {
                throw new ArbolInvalidoException("Línea " + numLinea + ": el año de nacimiento no es un número válido.");
            }

            Integer fallecimiento = null;
            if (!strFallecimiento.isEmpty()) {
                try {
                    fallecimiento = Integer.parseInt(strFallecimiento);
                } catch (NumberFormatException e) {
                    throw new ArbolInvalidoException("Línea " + numLinea + ": el año de fallecimiento no es un número válido.");
                }
            }

            Persona persona;
            try {
                persona = new Persona(posicion, nombre, nacimiento, fallecimiento);
                persona.validar();
            } catch (IllegalArgumentException e) {
                throw new ArbolInvalidoException("Línea " + numLinea + ": " + e.getMessage());
            }

            if ("1".equals(posicion)) {
                tieneSujeto = true;
            }

            posicionesVistas.add(posicion);
            registros.add(new RegistroPersona(numLinea, posicion, nombre, nacimiento, fallecimiento));
        }

        if (!tieneContenidoReal) {
            throw new ArbolInvalidoException("El texto está vacío.");
        }

        if (!tieneSujeto) {
            throw new ArbolInvalidoException("El texto no contiene al sujeto (posición '1').");
        }

        // Clasificar y ordenar las personas para asegurar que los dependientes se agreguen después
        RegistroPersona sujetoReg = null;
        List<RegistroPersona> ascendientesReg = new ArrayList<>();
        List<RegistroPersona> descendientesReg = new ArrayList<>();

        for (RegistroPersona reg : registros) {
            if ("1".equals(reg.posicion)) {
                sujetoReg = reg;
            } else if (Posicion.esAscendiente(reg.posicion)) {
                ascendientesReg.add(reg);
            } else if (Posicion.esDescendiente(reg.posicion)) {
                descendientesReg.add(reg);
            }
        }

        // Ascendientes ordenados por número Ahnentafel ascendente (2, 3, 4, 5...)
        ascendientesReg.sort(Comparator.comparingLong(r -> Long.parseLong(r.posicion)));

        // Descendientes ordenados por generación (cantidad de puntos) y estructura d'Aboville
        descendientesReg.sort((r1, r2) -> {
            String[] p1 = r1.posicion.split("\\.");
            String[] p2 = r2.posicion.split("\\.");
            if (p1.length != p2.length) {
                return Integer.compare(p1.length, p2.length);
            }
            for (int i = 0; i < p1.length; i++) {
                int n1 = Integer.parseInt(p1[i]);
                int n2 = Integer.parseInt(p2[i]);
                if (n1 != n2) {
                    return Integer.compare(n1, n2);
                }
            }
            return 0;
        });

        // Crear el árbol con el sujeto
        ArbolGenealogico arbol = new ArbolGenealogico(sujetoReg.persona);

        // Agregar ascendientes
        for (RegistroPersona reg : ascendientesReg) {
            try {
                arbol.agregar(reg.persona);
            } catch (Exception e) {
                throw new ArbolInvalidoException("Línea " + reg.numeroLinea + ": " + e.getMessage());
            }
        }

        // Agregar descendientes
        for (RegistroPersona reg : descendientesReg) {
            try {
                arbol.agregar(reg.persona);
            } catch (Exception e) {
                throw new ArbolInvalidoException("Línea " + reg.numeroLinea + ": " + e.getMessage());
            }
        }

        return arbol;
    }
}