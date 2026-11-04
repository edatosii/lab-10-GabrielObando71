/*
 * Dibuja un árbol genealógico en un <canvas>. Esta página YA está hecha: no es necesario modificarla.
 *
 *   const lienzo = dibujarArbol(document.getElementById("canvas"), miembros, { alSeleccionar: m => ... });
 *   lienzo.seleccionar("1.2");
 *
 * "miembros" es la lista que genera el servidor (util/Json.java):
 *   [{ posicion: "1", nombre: "Ana", nacimiento: 2000, fallecimiento: null }, ...]
 *
 * SOLO se dibujan los miembros que existen: no hay cajas vacías para los que faltan. Los ascendientes
 * (Ahnentafel: 2, 3, 4...) se dibujan ARRIBA del sujeto y los descendientes (d'Aboville: 1.1, 1.2...) ABAJO.
 */
(function (global) {
    "use strict";

    var ANCHO = 150, ALTO = 62, SEP_X = 24, SEP_Y = 52, MARGEN = 24;
    var COLORES = {
        sujeto:      { fondo: "#fff3d6", borde: "#c8741a" },
        ascendiente: { fondo: "#e3eefb", borde: "#2f6db5" },
        descendiente: { fondo: "#e2f4e6", borde: "#2f8a4a" }
    };

    function tipo(posicion) {
        if (posicion === "1") return "sujeto";
        return posicion.indexOf(".") >= 0 ? "descendiente" : "ascendiente";
    }

    // ---- 1. Construir los dos árboles a partir de las posiciones -------------------------------

    function construir(miembros) {
        var porPosicion = {};
        miembros.forEach(function (m) { porPosicion[m.posicion] = m; });

        function ascendiente(posicion) {
            var n = Number(posicion);
            var hijos = [];
            [String(2 * n), String(2 * n + 1)].forEach(function (p) {
                if (porPosicion[p]) hijos.push(ascendiente(p));
            });
            return { m: porPosicion[posicion], hijos: hijos };
        }

        function descendiente(posicion) {
            var hijos = miembros.filter(function (m) {
                return m.posicion.indexOf(posicion + ".") === 0 && m.posicion.indexOf(".", posicion.length + 1) < 0;
            }).sort(function (a, b) {
                return Number(a.posicion.split(".").pop()) - Number(b.posicion.split(".").pop());
            }).map(function (m) { return descendiente(m.posicion); });
            return { m: porPosicion[posicion], hijos: hijos };
        }

        var sujeto = porPosicion["1"];
        if (!sujeto) return null;
        var asc = ascendiente("1");
        var desc = descendiente("1");
        return { sujeto: sujeto, asc: asc, desc: desc };
    }

    // ---- 2. Calcular posiciones: cada persona queda centrada sobre sus hijos ---------------------

    function medir(nodo) {
        var x = 0;
        nodo.hijos.forEach(function (h) {
            medir(h);
            h.desde = x;
            x += h.ancho + SEP_X;
        });
        if (nodo.hijos.length === 0) {
            nodo.ancho = ANCHO;
            nodo.cx = ANCHO / 2;
        } else {
            nodo.ancho = x - SEP_X;
            var primero = nodo.hijos[0], ultimo = nodo.hijos[nodo.hijos.length - 1];
            nodo.cx = ((primero.desde + primero.cx) + (ultimo.desde + ultimo.cx)) / 2;
        }
    }

    function profundidad(nodo) {
        var max = 0;
        nodo.hijos.forEach(function (h) { max = Math.max(max, 1 + profundidad(h)); });
        return max;
    }

    // Coloca cada nodo en (x, y) absolutas; "sentido" es -1 para ascendientes (hacia arriba) y +1 para descendientes.
    function colocar(nodo, desdeX, y, sentido, cajas, lineas, padre) {
        nodo.x = desdeX + nodo.cx;
        nodo.y = y;
        cajas.push(nodo);
        if (padre) lineas.push({ de: padre, a: nodo, sentido: sentido });
        nodo.hijos.forEach(function (h) {
            colocar(h, desdeX + h.desde, y + sentido * (ALTO + SEP_Y), sentido, cajas, lineas, nodo);
        });
    }

    // ---- 3. Dibujar -------------------------------------------------------------------------------

    function recortar(ctx, texto, ancho) {
        if (ctx.measureText(texto).width <= ancho) return texto;
        while (texto.length > 1 && ctx.measureText(texto + "…").width > ancho) texto = texto.slice(0, -1);
        return texto + "…";
    }

    function anios(m) {
        return m.nacimiento + " – " + (m.fallecimiento == null ? "" : m.fallecimiento);
    }

    function etiqueta(posicion) {
        if (posicion === "1") return "Sujeto · 1";
        return (posicion.indexOf(".") >= 0 ? "d'Aboville " : "Ahnentafel ") + posicion;
    }

    function rect(ctx, x, y, w, h, r) {
        ctx.beginPath();
        ctx.moveTo(x + r, y);
        ctx.arcTo(x + w, y, x + w, y + h, r);
        ctx.arcTo(x + w, y + h, x, y + h, r);
        ctx.arcTo(x, y + h, x, y, r);
        ctx.arcTo(x, y, x + w, y, r);
        ctx.closePath();
    }

    global.dibujarArbol = function (canvas, miembros, opciones) {
        opciones = opciones || {};
        var datos = construir(miembros);
        var ctx = canvas.getContext("2d");
        var cajas = [], lineas = [], seleccionada = null;

        if (!datos) {
            canvas.width = 300; canvas.height = 60;
            ctx.font = "14px sans-serif";
            ctx.fillText("No hay miembros para dibujar.", 10, 34);
            return { seleccionar: function () {} };
        }

        medir(datos.asc);
        medir(datos.desc);
        var alturaAsc = profundidad(datos.asc);
        var alturaDesc = profundidad(datos.desc);
        // El sujeto de los dos árboles es la misma persona: se alinean por su centro.
        var centro = Math.max(datos.asc.cx, datos.desc.cx);
        var ySujeto = MARGEN + alturaAsc * (ALTO + SEP_Y);
        colocar(datos.asc, MARGEN + centro - datos.asc.cx, ySujeto, -1, cajas, lineas, null);
        // La raíz de los descendientes es el mismo sujeto (queda en la misma posición): no se dibuja dos veces.
        var descendientes = [];
        colocar(datos.desc, MARGEN + centro - datos.desc.cx, ySujeto, 1, descendientes, lineas, null);
        descendientes.slice(1).forEach(function (c) { cajas.push(c); });

        var ancho = Math.max(MARGEN + centro - datos.asc.cx + datos.asc.ancho,
                             MARGEN + centro - datos.desc.cx + datos.desc.ancho) + MARGEN;
        var alto = ySujeto + alturaDesc * (ALTO + SEP_Y) + ALTO + MARGEN;

        var escala = global.devicePixelRatio || 1;
        canvas.width = Math.ceil(ancho * escala);
        canvas.height = Math.ceil(alto * escala);
        canvas.style.width = ancho + "px";
        canvas.style.height = alto + "px";
        canvas.style.maxWidth = "none";

        function pintar() {
            ctx.setTransform(escala, 0, 0, escala, 0, 0);
            ctx.clearRect(0, 0, ancho, alto);
            ctx.lineWidth = 2;
            ctx.strokeStyle = "#9aa7b8";
            lineas.forEach(function (l) {
                var arriba = l.sentido === 1 ? l.de : l.a;      // la caja que queda más arriba
                var abajo = l.sentido === 1 ? l.a : l.de;
                var y1 = arriba.y + ALTO, y2 = abajo.y, ym = (y1 + y2) / 2;
                ctx.beginPath();
                ctx.moveTo(arriba.x, y1);
                ctx.lineTo(arriba.x, ym);
                ctx.lineTo(abajo.x, ym);
                ctx.lineTo(abajo.x, y2);
                ctx.stroke();
            });
            cajas.forEach(function (c) {
                var col = COLORES[tipo(c.m.posicion)];
                var x = c.x - ANCHO / 2, y = c.y;
                rect(ctx, x, y, ANCHO, ALTO, 8);
                ctx.fillStyle = col.fondo;
                ctx.fill();
                ctx.lineWidth = c.m.posicion === seleccionada ? 4 : 2;
                ctx.strokeStyle = c.m.posicion === seleccionada ? "#1f3a5f" : col.borde;
                ctx.stroke();
                ctx.textAlign = "center";
                ctx.fillStyle = "#1c2733";
                ctx.font = "bold 14px system-ui, sans-serif";
                ctx.fillText(recortar(ctx, c.m.nombre, ANCHO - 14), c.x, y + 23);
                ctx.font = "12px system-ui, sans-serif";
                ctx.fillStyle = "#3b4a5c";
                ctx.fillText(anios(c.m), c.x, y + 40);
                ctx.font = "11px system-ui, sans-serif";
                ctx.fillStyle = "#6b7a8c";
                ctx.fillText(etiqueta(c.m.posicion), c.x, y + 54);
            });
        }

        function seleccionar(posicion) {
            var caja = cajas.filter(function (c) { return c.m.posicion === posicion; })[0];
            if (!caja) return;
            seleccionada = posicion;
            pintar();
            if (opciones.alSeleccionar) opciones.alSeleccionar(caja.m);
        }

        canvas.addEventListener("click", function (e) {
            var r = canvas.getBoundingClientRect();
            var px = (e.clientX - r.left) * (ancho / r.width);
            var py = (e.clientY - r.top) * (alto / r.height);
            for (var i = 0; i < cajas.length; i++) {
                var c = cajas[i];
                if (px >= c.x - ANCHO / 2 && px <= c.x + ANCHO / 2 && py >= c.y && py <= c.y + ALTO) {
                    seleccionar(c.m.posicion);
                    return;
                }
            }
        });
        canvas.style.cursor = "pointer";

        pintar();
        return { seleccionar: seleccionar };
    };
})(window);
