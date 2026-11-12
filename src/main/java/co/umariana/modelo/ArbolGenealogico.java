package co.umariana.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El árbol genealógico: un SUJETO (posición "1") con sus ASCENDIENTES (padres, abuelos... en Ahnentafel)
 * y sus DESCENDIENTES (hijos, nietos... en d'Aboville). Ver la clase Posicion.
 *
 * Son en realidad dos árboles unidos en la raíz:
 *
 *    ascendientes:  árbol BINARIO. Cada persona tiene como mucho un padre (2n) y una madre (2n + 1).
 *    descendientes: árbol GENERAL. Cada persona tiene una lista de hijos (1.1, 1.2, ...) ordenada por número.
 *
 * El árbol solo guarda a los miembros que EXISTEN: no hay "huecos" ni personas vacías. Puede faltar el padre
 * de alguien, o el hijo 1.1 aunque exista el 1.2.
 */
public class ArbolGenealogico {

    /**
     * Clase interna Nodo para representar cada miembro y sus enlaces en el árbol genealógico.
     */
    public static class Nodo {
        private Persona persona;
        private Nodo padre;
        private Nodo madre;
        private List<Nodo> hijos;

        public Nodo(Persona persona) {
            this.persona = persona;
            this.padre = null;
            this.madre = null;
            this.hijos = new ArrayList<>();
        }

        public Persona getPersona() { return persona; }
        public void setPersona(Persona persona) { this.persona = persona; }
        public Nodo getPadre() { return padre; }
        public void setPadre(Nodo padre) { this.padre = padre; }
        public Nodo getMadre() { return madre; }
        public void setMadre(Nodo madre) { this.madre = madre; }
        public List<Nodo> getHijos() { return hijos; }
    }

    private Nodo raiz;
    private int cantidadMiembros;

    /**
     * Crea un árbol que solo tiene al sujeto.
     *
     * @throws IllegalArgumentException si la persona no es válida (Persona.validar)
     * @throws ArbolInvalidoException si su posición no es "1"
     */
    public ArbolGenealogico(Persona sujeto) {
        if (sujeto == null) {
            throw new IllegalArgumentException("El sujeto no puede ser nulo.");
        }
        sujeto.validar();
        if (!"1".equals(sujeto.getPosicion())) {
            throw new ArbolInvalidoException("La posición del sujeto debe ser exactamente '1'.");
        }
        this.raiz = new Nodo(sujeto);
        this.cantidadMiembros = 1;
    }

    /** @return el sujeto (la persona de la posición "1") */
    public Persona sujeto() {
        return raiz != null ? raiz.getPersona() : null;
    }

    /** @return cuántos miembros tiene el árbol (el sujeto cuenta; un árbol nuevo tiene 1) */
    public int tamano() {
        return cantidadMiembros;
    }

    /**
     * @return la persona de esa posición, o null si no existe en el árbol o si la posición no es válida
     */
    public Persona buscar(String posicion) {
        Nodo nodo = buscarNodo(posicion);
        return nodo != null ? nodo.getPersona() : null;
    }

    /**
     * Método auxiliar para buscar un Nodo siguiendo las reglas de camino (Ahnentafel o d'Aboville).
     */
    private Nodo buscarNodo(String posicion) {
        if (!Posicion.esValida(posicion)) {
            return null;
        }
        posicion = posicion.trim();
        if ("1".equals(posicion)) {
            return raiz;
        }

        // Si es Ahnentafel (Ascendiente)
        if (posicion.matches("\\d+")) {
            long n = Long.parseLong(posicion);
            String binario = Long.toBinaryString(n);
            Nodo actual = raiz;
            for (int i = 1; i < binario.length(); i++) {
                if (actual == null) return null;
                char bit = binario.charAt(i);
                if (bit == '0') {
                    actual = actual.getPadre();
                } else {
                    actual = actual.getMadre();
                }
            }
            return actual;
        } 
        // Si es d'Aboville (Descendiente)
        else {
            String[] partes = posicion.split("\\.");
            if (partes.length == 0 || !"1".equals(partes[0])) {
                return null;
            }
            Nodo actual = raiz;
            for (int i = 1; i < partes.length; i++) {
                if (actual == null) return null;
                int numHijo;
                try {
                    numHijo = Integer.parseInt(partes[i]);
                } catch (NumberFormatException e) {
                    return null;
                }
                
                Nodo hijoEncontrado = null;
                for (Nodo h : actual.getHijos()) {
                    if (Posicion.orden(h.getPersona().getPosicion()) == numHijo) {
                        hijoEncontrado = h;
                        break;
                    }
                }
                actual = hijoEncontrado;
            }
            return actual;
        }
    }

    /** @return true si hay alguien en esa posición (false también si la posición no es válida) */
    public boolean contiene(String posicion) {
        return buscarNodo(posicion) != null;
    }

    /**
     * Agrega una persona en la posición que ella misma trae.
     */
    public Persona agregar(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser nula.");
        }
        persona.validar();
        String pos = persona.getPosicion();

        if (!Posicion.esValida(pos) || "1".equals(pos)) {
            throw new ArbolInvalidoException("La posición no es válida para agregar o ya es el sujeto.");
        }
        if (contiene(pos)) {
            throw new ArbolInvalidoException("La posición ya existe en el árbol: " + pos);
        }

        if (Posicion.esAscendiente(pos)) {
            String posHijo = Posicion.posicionDelHijoEnLinea(pos);
            Nodo nodoHijo = buscarNodo(posHijo);
            if (nodoHijo == null) {
                throw new ArbolInvalidoException("Falta la persona de la que depende (hijo): " + posHijo);
            }
            if (persona.getNacimiento() >= nodoHijo.getPersona().getNacimiento()) {
                throw new ArbolInvalidoException("El ascendiente debe haber nacido antes que su hijo.");
            }

            Nodo nuevoNodo = new Nodo(persona);
            long n = Long.parseLong(pos);
            if (n % 2 == 0) {
                if (nodoHijo.getPadre() != null) {
                    throw new ArbolInvalidoException("El padre ya existe.");
                }
                nodoHijo.setPadre(nuevoNodo);
            } else {
                if (nodoHijo.getMadre() != null) {
                    throw new ArbolInvalidoException("La madre ya existe.");
                }
                nodoHijo.setMadre(nuevoNodo);
            }
        } else if (Posicion.esDescendiente(pos)) {
            String posProgenitor = Posicion.posicionDelProgenitor(pos);
            Nodo nodoProgenitor = buscarNodo(posProgenitor);
            if (nodoProgenitor == null) {
                throw new ArbolInvalidoException("Falta el progenitor: " + posProgenitor);
            }
            if (persona.getNacimiento() <= nodoProgenitor.getPersona().getNacimiento()) {
                throw new ArbolInvalidoException("El descendiente debe haber nacido después que su progenitor.");
            }

            Nodo nuevoNodo = new Nodo(persona);
            nodoProgenitor.getHijos().add(nuevoNodo);
        } else {
            throw new ArbolInvalidoException("Posición no soportada para agregar.");
        }

        cantidadMiembros++;
        return persona;
    }

    /**
     * Agrega el PADRE de la persona de esa posición, en la posición 2n.
     */
    public Persona agregarPadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo nodoHijo = buscarNodo(posicion);
        if (nodoHijo == null) {
            throw new ArbolInvalidoException("La posición no existe en el árbol: " + posicion);
        }
        if (!"1".equals(posicion) && !Posicion.esAscendiente(posicion)) {
            throw new ArbolInvalidoException("Solo se pueden agregar padres al sujeto y a sus ascendientes.");
        }
        if (nodoHijo.getPadre() != null) {
            throw new ArbolInvalidoException("El padre ya existe en esta posición.");
        }

        String posPadre = Posicion.posicionDelPadre(posicion);
        Persona padre = new Persona(posPadre, nombre, nacimiento, fallecimiento);
        return agregar(padre);
    }

    /**
     * Igual que agregarPadre, pero agrega la MADRE (posición 2n + 1).
     */
    public Persona agregarMadre(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo nodoHijo = buscarNodo(posicion);
        if (nodoHijo == null) {
            throw new ArbolInvalidoException("La posición no existe en el árbol: " + posicion);
        }
        if (!"1".equals(posicion) && !Posicion.esAscendiente(posicion)) {
            throw new ArbolInvalidoException("Solo se pueden agregar madres al sujeto y a sus ascendientes.");
        }
        if (nodoHijo.getMadre() != null) {
            throw new ArbolInvalidoException("La madre ya existe en esta posición.");
        }

        String posMadre = Posicion.posicionDeLaMadre(posicion);
        Persona madre = new Persona(posMadre, nombre, nacimiento, fallecimiento);
        return agregar(madre);
    }

    /**
     * Agrega un HIJO a la persona de esa posición.
     */
    public Persona agregarHijo(String posicion, String nombre, int nacimiento, Integer fallecimiento) {
        Nodo nodoProgenitor = buscarNodo(posicion);
        if (nodoProgenitor == null) {
            throw new ArbolInvalidoException("La posición no existe en el árbol: " + posicion);
        }
        if (!"1".equals(posicion) && !Posicion.esDescendiente(posicion)) {
            throw new ArbolInvalidoException("Solo se pueden agregar hijos al sujeto y a sus descendientes.");
        }

        int maxOrden = 0;
        for (Nodo h : nodoProgenitor.getHijos()) {
            int ord = Posicion.orden(h.getPersona().getPosicion());
            if (ord > maxOrden) {
                maxOrden = ord;
            }
        }
        String posHijo = posicion + "." + (maxOrden + 1);
        Persona hijo = new Persona(posHijo, nombre, nacimiento, fallecimiento);
        return agregar(hijo);
    }

    /**
     * Elimina a la persona de esa posición y TODO lo que depende de ella.
     */
    public int eliminar(String posicion) {
        if ("1".equals(posicion)) {
            throw new ArbolInvalidoException("El sujeto ('1') no se puede eliminar.");
        }
        Nodo nodoAEliminar = buscarNodo(posicion);
        if (nodoAEliminar == null) {
            throw new ArbolInvalidoException("La posición no existe en el árbol: " + posicion);
        }

        if (Posicion.esAscendiente(posicion)) {
            String posHijo = Posicion.posicionDelHijoEnLinea(posicion);
            Nodo nodoHijo = buscarNodo(posHijo);
            if (nodoHijo != null) {
                long n = Long.parseLong(posicion);
                if (n % 2 == 0) {
                    nodoHijo.setPadre(null);
                } else {
                    nodoHijo.setMadre(null);
                }
            }
        } else if (Posicion.esDescendiente(posicion)) {
            String posProg = Posicion.posicionDelProgenitor(posicion);
            Nodo nodoProg = buscarNodo(posProg);
            if (nodoProg != null) {
                nodoProg.getHijos().removeIf(h -> h.getPersona().getPosicion().equals(posicion));
            }
        }

        int eliminados = contarYDesconectarSubarbol(nodoAEliminar);
        cantidadMiembros -= eliminados;
        return eliminados;
    }

    private int contarYDesconectarSubarbol(Nodo nodo) {
        if (nodo == null) return 0;
        int count = 1;
        if (nodo.getPadre() != null) {
            count += contarYDesconectarSubarbol(nodo.getPadre());
            nodo.setPadre(null);
        }
        if (nodo.getMadre() != null) {
            count += contarYDesconectarSubarbol(nodo.getMadre());
            nodo.setMadre(null);
        }
        for (Nodo h : new ArrayList<>(nodo.getHijos())) {
            count += contarYDesconectarSubarbol(h);
        }
        nodo.getHijos().clear();
        return count;
    }

    /**
     * @return todos los miembros en este orden: el sujeto; luego los ascendientes por número Ahnentafel
     *         ascendente (2, 3, 4, 5...); luego los descendientes en PREORDEN.
     */
    public List<Persona> miembros() {
        List<Persona> resultado = new ArrayList<>();
        if (raiz == null) return resultado;

        resultado.add(raiz.getPersona());
        resultado.addAll(ascendientes());
        resultado.addAll(descendientes());

        return resultado;
    }

    /** @return solo los ascendientes, por número Ahnentafel ascendente (sin el sujeto) */
    public List<Persona> ascendientes() {
        List<Persona> resultado = new ArrayList<>();
        if (raiz == null) return resultado;

        List<NodoConId> listaAsc = new ArrayList<>();
        recolectarAscendientes(raiz, 1L, listaAsc);
        
        listaAsc.sort((a, b) -> Long.compare(a.id, b.id));
        
        for (NodoConId item : listaAsc) {
            if (item.id > 1) {
                resultado.add(item.nodo.getPersona());
            }
        }
        return resultado;
    }

    private static class NodoConId {
        long id;
        Nodo nodo;
        NodoConId(long id, Nodo nodo) {
            this.id = id;
            this.nodo = nodo;
        }
    }

    private void recolectarAscendientes(Nodo actual, long id, List<NodoConId> lista) {
        if (actual == null) return;
        lista.add(new NodoConId(id, actual));
        if (actual.getPadre() != null) {
            recolectarAscendientes(actual.getPadre(), id * 2, lista);
        }
        if (actual.getMadre() != null) {
            recolectarAscendientes(actual.getMadre(), (id * 2) + 1, lista);
        }
    }

   /** @return solo los descendientes, en preorden (sin el sujeto) */
    public List<Persona> descendientes() {
        List<Persona> resultado = new ArrayList<>();
        if (raiz == null) return resultado;
        
        // Ordenar los hijos directos del sujeto de menor a mayor (1.1, 1.2, ...)
        List<Nodo> hijosRaiz = new ArrayList<>(raiz.getHijos());
        hijosRaiz.sort((h1, h2) -> compararDAboville(h1.getPersona().getPosicion(), h2.getPersona().getPosicion()));

        for (Nodo hijo : hijosRaiz) {
            recolectarDescendientesPreorden(hijo, resultado);
        }
        return resultado;
    }

    private void recolectarDescendientesPreorden(Nodo nodo, List<Persona> lista) {
        if (nodo == null) return;
        lista.add(nodo.getPersona());
        
        // Ordenar los hijos de este nodo también de forma ascendente
        List<Nodo> hijosOrdenados = new ArrayList<>(nodo.getHijos());
        hijosOrdenados.sort((h1, h2) -> compararDAboville(h1.getPersona().getPosicion(), h2.getPersona().getPosicion()));
        
        for (Nodo hijo : hijosOrdenados) {
            recolectarDescendientesPreorden(hijo, lista);
        }
    }

    private int compararDAboville(String pos1, String pos2) {
        String[] partes1 = pos1.split("\\.");
        String[] partes2 = pos2.split("\\.");
        int minLen = Math.min(partes1.length, partes2.length);
        for (int i = 0; i < minLen; i++) {
            int num1 = Integer.parseInt(partes1[i]);
            int num2 = Integer.parseInt(partes2[i]);
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return Integer.compare(partes1.length, partes2.length);
    }
}