<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.umariana.modelo.ArbolGenealogico, co.umariana.util.Html, co.umariana.util.Json" %>
<%
    request.setAttribute("seccion", "construir");
    ArbolGenealogico arbol = (ArbolGenealogico) request.getAttribute("arbol");
    String texto = (String) request.getAttribute("texto");
    String nombre = (String) request.getAttribute("nombre");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Construir árbol genealógico</title>
    <link rel="stylesheet" href="css/estilos.css">
</head>
<body>
<%@ include file="_menu.jspf" %>
<main>
    <h1>Construir árbol genealógico</h1>
    <p class="sub">Escriba una persona por línea y construya el árbol.</p>

    <% if (error != null) { %><p class="mensaje error"><%= Html.escapar(error) %></p><% } %>

    <div class="tarjeta">
        <h2>Personas</h2>
        <p class="ayuda">
            Cada línea: <code>posición;nombre;año de nacimiento;año de fallecimiento</code> (el fallecimiento puede quedar vacío).
            Los <strong>ascendientes</strong> usan el sistema <strong>Ahnentafel</strong>: <code>1</code> es el sujeto, <code>2</code> su padre,
            <code>3</code> su madre, <code>4</code> el padre del 2... (el padre de <em>n</em> es <em>2n</em> y la madre <em>2n+1</em>).
            Los <strong>descendientes</strong> usan <strong>d'Aboville</strong>: <code>1.1</code> es el primer hijo del sujeto,
            <code>1.2</code> el segundo, <code>1.1.1</code> el primer hijo del <code>1.1</code>...
        </p>
        <form method="post" action="construir">
            <input type="hidden" name="accion" value="previsualizar">
            <textarea id="texto" name="texto" spellcheck="false" placeholder="1;John Doe;2002;"><%= Html.escapar(texto) %></textarea>
            <div class="acciones">
                <button class="boton" type="submit">Construir árbol genealógico</button>
                <button class="boton suave" type="button" id="ejemplo">Cargar un ejemplo</button>
            </div>
        </form>
    </div>

    <% if (arbol != null) { %>
    <div class="tarjeta">
        <h2>Vista previa (<%= arbol.tamano() %> <%= arbol.tamano() == 1 ? "miembro" : "miembros" %>)</h2>
        <div class="lienzo"><canvas id="canvas"></canvas></div>
        <p class="leyenda"><span class="l-sujeto">Sujeto</span><span class="l-asc">Ascendientes (Ahnentafel)</span><span class="l-desc">Descendientes (d'Aboville)</span></p>

        <form method="post" action="construir" style="margin-top:1rem">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="texto" value="<%= Html.escapar(texto) %>">
            <label for="nombre">Nombre del árbol</label>
            <input type="text" id="nombre" name="nombre" maxlength="100" required
                   value="<%= Html.escapar(nombre == null || nombre.isEmpty() ? "Árbol de " + arbol.sujeto().getNombre() : nombre) %>">
            <div class="acciones"><button class="boton acento" type="submit">Guardar en base de datos</button></div>
        </form>
    </div>
    <script src="js/arbol.js"></script>
    <script>dibujarArbol(document.getElementById("canvas"), <%= Json.miembros(arbol.miembros()) %>, {});</script>
    <% } %>
</main>
<script>
    document.getElementById("ejemplo").addEventListener("click", function () {
        document.getElementById("texto").value =
            "1;John Doe;2002;\n2;Henry Doe;1920;1990\n3;Maria Doe;1921;\n4;Peter Doe;1890;1950\n5;Lucy Doe;1895;1960\n" +
            "1.1;Timmy Doe;2021;\n1.2;Amy Doe;2026;\n1.1.1;Baby Doe;2025;\n";
    });
</script>
</body>
</html>
