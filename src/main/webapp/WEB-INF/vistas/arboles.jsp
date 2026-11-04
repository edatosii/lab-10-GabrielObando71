<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.umariana.modelo.ResumenArbol, co.umariana.util.Html, java.util.List, java.time.format.DateTimeFormatter" %>
<%
    request.setAttribute("seccion", "arboles");
    List<ResumenArbol> arboles = (List<ResumenArbol>) request.getAttribute("arboles");
    String ok = (String) request.getAttribute("ok");
    String error = (String) request.getAttribute("error");
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Árboles guardados</title>
    <link rel="stylesheet" href="css/estilos.css">
</head>
<body>
<%@ include file="_menu.jspf" %>
<main>
    <h1>Árboles guardados</h1>
    <p class="sub">Los árboles que están en la base de datos.</p>

    <% if (ok != null) { %><p class="mensaje ok"><%= Html.escapar(ok) %></p><% } %>
    <% if (error != null) { %><p class="mensaje error"><%= Html.escapar(error) %></p><% } %>

    <div class="tarjeta">
        <% if (arboles == null || arboles.isEmpty()) { %>
            <p>Todavía no hay árboles guardados. <a href="construir">Construya el primero</a>.</p>
        <% } else { %>
            <table>
                <thead><tr><th>Nombre</th><th>Miembros</th><th>Creado</th><th></th></tr></thead>
                <tbody>
                <% for (ResumenArbol a : arboles) { %>
                    <tr>
                        <td><a href="arbol?id=<%= a.id() %>"><strong><%= Html.escapar(a.nombre()) %></strong></a></td>
                        <td><%= a.totalMiembros() %></td>
                        <td><%= a.creado() == null ? "" : a.creado().format(formato) %></td>
                        <td style="text-align:right; white-space:nowrap">
                            <a class="boton suave" href="arbol?id=<%= a.id() %>">Abrir</a>
                            <form method="post" action="arboles" style="display:inline"
                                  onsubmit="return confirm('¿Eliminar este árbol y todos sus miembros?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="<%= a.id() %>">
                                <button class="boton peligro" type="submit">Eliminar</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>
</main>
</body>
</html>
