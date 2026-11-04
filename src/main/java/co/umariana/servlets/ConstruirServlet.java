package co.umariana.servlets;

import co.umariana.construccion.ConstructorArbol;
import co.umariana.dao.ArbolDAO;
import co.umariana.modelo.ArbolGenealogico;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Página "Construir árbol genealógico" (/construir): el usuario escribe una persona por línea, se construye el
 * árbol con ConstructorArbol y se muestra una vista previa; desde la vista previa se guarda con ArbolDAO.
 *
 *   GET  /construir                       -> formulario vacío
 *   POST /construir  accion=previsualizar -> construye el árbol y lo dibuja (sin guardar nada)
 *   POST /construir  accion=guardar       -> construye, guarda en la base de datos y va a /arbol?id=...
 *
 * Esta clase YA está completa: no es necesario modificarla.
 */
@WebServlet("/construir")
public class ConstruirServlet extends HttpServlet {

    private final ArbolDAO dao = new ArbolDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("nombre", "");
        req.setAttribute("texto", "");
        req.getRequestDispatcher("/WEB-INF/vistas/construir.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String accion = req.getParameter("accion");
        String nombre = req.getParameter("nombre") == null ? "" : req.getParameter("nombre").trim();
        String texto = req.getParameter("texto") == null ? "" : req.getParameter("texto");
        req.setAttribute("nombre", nombre);
        req.setAttribute("texto", texto);
        try {
            ArbolGenealogico arbol = ConstructorArbol.construir(texto);
            if ("guardar".equals(accion)) {
                if (nombre.isEmpty()) {
                    throw new IllegalArgumentException("Escriba un nombre para el árbol antes de guardarlo.");
                }
                long id = dao.guardar(nombre, arbol);
                if (id < 0) {
                    throw new IllegalArgumentException("No se pudo guardar en la base de datos "
                            + "(revise que el nombre tenga máximo 100 caracteres y que MySQL esté funcionando).");
                }
                resp.sendRedirect(req.getContextPath() + "/arbol?id=" + id + "&ok="
                        + URLEncoder.encode("Árbol guardado.", StandardCharsets.UTF_8));
                return;
            }
            req.setAttribute("arbol", arbol);
        } catch (UnsupportedOperationException e) {
            req.setAttribute("error", "Todavía falta implementar: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/vistas/construir.jsp").forward(req, resp);
    }
}
