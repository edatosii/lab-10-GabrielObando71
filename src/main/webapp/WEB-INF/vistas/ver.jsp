<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.umariana.modelo.ArbolGenealogico, co.umariana.modelo.Persona, co.umariana.util.Html, co.umariana.util.Json" %>
<%
    request.setAttribute("seccion", "arboles");
    ArbolGenealogico arbol = (ArbolGenealogico) request.getAttribute("arbol");
    Object id = request.getAttribute("id");
    String nombreArbol = (String) request.getAttribute("nombreArbol");
    String ok = (String) request.getAttribute("ok");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= Html.escapar(nombreArbol == null ? "Árbol" : nombreArbol) %></title>
    <link rel="stylesheet" href="css/estilos.css">
</head>
<body>
<%@ include file="_menu.jspf" %>
<main>
    <h1><%= Html.escapar(nombreArbol == null ? "Árbol" : nombreArbol) %></h1>
    <p class="sub"><a href="arboles">&larr; Volver a los árboles guardados</a></p>

    <% if (ok != null) { %><p class="mensaje ok"><%= Html.escapar(ok) %></p><% } %>
    <% if (error != null) { %><p class="mensaje error"><%= Html.escapar(error) %></p><% } %>

    <% if (arbol != null) { %>
    <div class="tarjeta">
        <h2><%= arbol.tamano() %> <%= arbol.tamano() == 1 ? "miembro" : "miembros" %></h2>
        <div class="lienzo"><canvas id="canvas"></canvas></div>
        <p class="leyenda"><span class="l-sujeto">Sujeto</span><span class="l-asc">Ascendientes (Ahnentafel)</span><span class="l-desc">Descendientes (d'Aboville)</span></p>
        <p class="ayuda">Haga clic en una persona del dibujo (o en una fila de la tabla) para agregarle familiares o eliminarla.</p>
    </div>

    <div class="tarjeta panel oculto" id="panel">
        <h2 id="panel-titulo"></h2>

        <form method="post" action="arbol" id="form-agregar">
            <input type="hidden" name="id" value="<%= id %>">
            <input type="hidden" name="posicion" id="f-posicion">
            <input type="hidden" name="accion" id="f-accion">
            <div class="fila">
                <div><label for="f-nombre">Nombre</label><input type="text" id="f-nombre" name="nombre" maxlength="100" required></div>
                <div><label for="f-nacimiento">Nacimiento</label><input type="number" id="f-nacimiento" name="nacimiento" required></div>
                <div><label for="f-fallecimiento">Fallecimiento (opcional)</label><input type="number" id="f-fallecimiento" name="fallecimiento"></div>
            </div>
            <div class="acciones">
                <button class="boton" type="submit" data-accion="agregarPadre" id="b-padre">Agregar padre</button>
                <button class="boton" type="submit" data-accion="agregarMadre" id="b-madre">Agregar madre</button>
                <button class="boton" type="submit" data-accion="agregarHijo" id="b-hijo">Agregar hijo/a</button>
            </div>
        </form>

        <form method="post" action="arbol" id="form-eliminar" style="margin-top:1rem"
              onsubmit="return confirm('Se eliminará a esta persona y todos sus ascendientes o descendientes. ¿Continuar?');">
            <input type="hidden" name="id" value="<%= id %>">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="posicion" id="e-posicion">
            <button class="boton peligro" type="submit" id="b-eliminar">Eliminar</button>
        </form>
    </div>

    <div class="tarjeta">
        <h2>Miembros</h2>
        <table>
            <thead><tr><th>Posición</th><th>Nombre</th><th>Nacimiento</th><th>Fallecimiento</th></tr></thead>
            <tbody>
            <% for (Persona p : arbol.miembros()) { %>
                <tr class="fila-miembro" data-posicion="<%= Html.escapar(p.getPosicion()) %>">
                    <td><code><%= Html.escapar(p.getPosicion()) %></code></td>
                    <td><%= Html.escapar(p.getNombre()) %></td>
                    <td><%= p.getNacimiento() %></td>
                    <td><%= p.getFallecimiento() == null ? "" : p.getFallecimiento() %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <script src="js/arbol.js"></script>
    <script>
        var miembros = <%= Json.miembros(arbol.miembros()) %>;
        var existentes = {};
        miembros.forEach(function (m) { existentes[m.posicion] = m; });

        function esAscendiente(p) { return /^[0-9]+$/.test(p); }   // Ahnentafel (incluye el sujeto "1")

        function elegir(m) {
            var p = m.posicion;
            var asc = esAscendiente(p);
            var sujeto = p === "1";
            var n = asc ? parseInt(p, 10) : 0;
            document.getElementById("panel").classList.remove("oculto");
            document.getElementById("panel-titulo").textContent = m.nombre + " (posición " + p + ")";
            document.getElementById("f-posicion").value = p;
            document.getElementById("e-posicion").value = p;
            // Solo se agregan padres a ascendientes (y al sujeto) y que todavía no los tengan; hijos al sujeto y a los descendientes.
            document.getElementById("b-padre").style.display = asc && !existentes[String(2 * n)] ? "" : "none";
            document.getElementById("b-madre").style.display = asc && !existentes[String(2 * n + 1)] ? "" : "none";
            document.getElementById("b-hijo").style.display = sujeto || !asc ? "" : "none";
            document.getElementById("form-eliminar").style.display = sujeto ? "none" : "";
            document.querySelectorAll("tr.fila-miembro").forEach(function (tr) {
                tr.classList.toggle("seleccionada", tr.getAttribute("data-posicion") === p);
            });
        }

        var dibujo = dibujarArbol(document.getElementById("canvas"), miembros, { alSeleccionar: elegir });

        document.querySelectorAll("tr.fila-miembro").forEach(function (tr) {
            tr.addEventListener("click", function () { dibujo.seleccionar(tr.getAttribute("data-posicion")); });
        });
        document.querySelectorAll("#form-agregar button[data-accion]").forEach(function (b) {
            b.addEventListener("click", function () { document.getElementById("f-accion").value = b.getAttribute("data-accion"); });
        });
    </script>
    <% } %>
</main>
</body>
</html>
