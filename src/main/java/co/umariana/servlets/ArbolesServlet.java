package co.umariana.servlets;

import co.umariana.dao.ArbolDAO;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lista los árboles guardados (/arboles) y permite eliminarlos.
 *
 *   GET  /arboles                       -> la tabla de árboles (ArbolDAO.listar)
 *   POST /arboles  accion=eliminar&id=N -> elimina el árbol (ArbolDAO.eliminar) y vuelve a la lista
 *
 * Esta clase YA está completa: no es necesario modificarla.
 */
@WebServlet("/arboles")
public class ArbolesServlet extends HttpServlet {

    private final ArbolDAO dao = new ArbolDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("arboles", dao.listar());
        } catch (UnsupportedOperationException e) {
            req.setAttribute("error", "Todavía falta implementar: " + e.getMessage());
        }
        req.setAttribute("ok", req.getParameter("ok"));
        if (req.getParameter("error") != null) {
            req.setAttribute("error", req.getParameter("error"));
        }
        req.getRequestDispatcher("/WEB-INF/vistas/arboles.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String mensaje;
        String clave = "ok";
        try {
            long id = Long.parseLong(req.getParameter("id"));
            if (dao.eliminar(id)) {
                mensaje = "Árbol eliminado.";
            } else {
                clave = "error";
                mensaje = "No se pudo eliminar el árbol (ya no existe o hubo un error).";
            }
        } catch (NumberFormatException e) {
            clave = "error";
            mensaje = "Árbol inválido.";
        } catch (UnsupportedOperationException e) {
            clave = "error";
            mensaje = "Todavía falta implementar: " + e.getMessage();
        }
        resp.sendRedirect(req.getContextPath() + "/arboles?" + clave + "=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
    }
}
