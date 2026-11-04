package co.umariana.servlets;

import co.umariana.dao.ArbolDAO;
import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ResumenArbol;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Un árbol guardado (/arbol?id=N): se dibuja y se puede modificar.
 *
 *   GET  /arbol?id=N                          -> dibuja el árbol (ArbolDAO.cargar)
 *   POST /arbol  accion=agregarPadre   id, posicion, nombre, nacimiento, fallecimiento
 *   POST /arbol  accion=agregarMadre   (igual)
 *   POST /arbol  accion=agregarHijo    (igual)
 *   POST /arbol  accion=eliminar       id, posicion   (elimina también todo lo que depende de esa persona)
 *
 * Cada POST carga el árbol, aplica el cambio con los métodos de ArbolGenealogico, lo guarda con
 * ArbolDAO.actualizar y vuelve al GET. Si algo no cumple las reglas, muestra el mensaje de error.
 *
 * Esta clase YA está completa: no es necesario modificarla.
 */
@WebServlet("/arbol")
public class ArbolServlet extends HttpServlet {

    private final ArbolDAO dao = new ArbolDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/arboles?error=" + codificar("Árbol inválido."));
            return;
        }
        try {
            ArbolGenealogico arbol = dao.cargar(id);
            if (arbol == null) {
                resp.sendRedirect(req.getContextPath() + "/arboles?error=" + codificar("Ese árbol no existe."));
                return;
            }
            String nombre = "Árbol " + id;
            for (ResumenArbol r : dao.listar()) {
                if (r.id() == id) {
                    nombre = r.nombre();
                }
            }
            req.setAttribute("id", id);
            req.setAttribute("nombreArbol", nombre);
            req.setAttribute("arbol", arbol);
        } catch (UnsupportedOperationException e) {
            req.setAttribute("error", "Todavía falta implementar: " + e.getMessage());
        }
        req.setAttribute("ok", req.getParameter("ok"));
        if (req.getParameter("error") != null) {
            req.setAttribute("error", req.getParameter("error"));
        }
        req.getRequestDispatcher("/WEB-INF/vistas/ver.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String id = req.getParameter("id");
        String clave = "ok";
        String mensaje;
        try {
            long arbolId = Long.parseLong(id);
            ArbolGenealogico arbol = dao.cargar(arbolId);
            if (arbol == null) {
                throw new IllegalArgumentException("Ese árbol no existe.");
            }
            String posicion = req.getParameter("posicion");
            String accion = req.getParameter("accion") == null ? "" : req.getParameter("accion");
            switch (accion) {
                case "agregarPadre" -> {
                    var p = arbol.agregarPadre(posicion, req.getParameter("nombre").trim(), entero(req, "nacimiento"), opcional(req));
                    mensaje = "Se agregó a " + p.getNombre() + " (posición " + p.getPosicion() + ").";
                }
                case "agregarMadre" -> {
                    var p = arbol.agregarMadre(posicion, req.getParameter("nombre").trim(), entero(req, "nacimiento"), opcional(req));
                    mensaje = "Se agregó a " + p.getNombre() + " (posición " + p.getPosicion() + ").";
                }
                case "agregarHijo" -> {
                    var p = arbol.agregarHijo(posicion, req.getParameter("nombre").trim(), entero(req, "nacimiento"), opcional(req));
                    mensaje = "Se agregó a " + p.getNombre() + " (posición " + p.getPosicion() + ").";
                }
                case "eliminar" -> {
                    int borrados = arbol.eliminar(posicion);
                    mensaje = "Se eliminaron " + borrados + (borrados == 1 ? " persona." : " personas.");
                }
                default -> throw new IllegalArgumentException("Acción desconocida.");
            }
            if (!dao.actualizar(arbolId, arbol)) {
                throw new IllegalArgumentException("No se pudo guardar el cambio en la base de datos.");
            }
        } catch (NumberFormatException e) {
            clave = "error";
            mensaje = "Los años y el árbol deben ser números enteros.";
        } catch (UnsupportedOperationException e) {
            clave = "error";
            mensaje = "Todavía falta implementar: " + e.getMessage();
        } catch (IllegalArgumentException | NullPointerException e) {
            clave = "error";
            mensaje = e instanceof NullPointerException ? "Faltan datos del formulario." : e.getMessage();
        }
        resp.sendRedirect(req.getContextPath() + "/arbol?id=" + codificar(id == null ? "" : id) + "&" + clave + "=" + codificar(mensaje));
    }

    private static int entero(HttpServletRequest req, String campo) {
        return Integer.parseInt(req.getParameter(campo).trim());
    }

    /** El fallecimiento puede venir vacío: es "no tiene". */
    private static Integer opcional(HttpServletRequest req) {
        String f = req.getParameter("fallecimiento");
        return f == null || f.isBlank() ? null : Integer.valueOf(f.trim());
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }
}
