# [LAB-10]: Árboles genealógicos con Ahnentafel, d'Aboville y MySQL

## Prerequisitos

Los mismos del laboratorio 9: **Git**, **Visual Studio Code**, cuenta de **GitHub**, **JDK 17 o superior** (`java -version` y `javac -version`) y **MySQL Server 8**. Si usa Mac: `brew install --cask temurin@17`.

## Descripción del laboratorio

Aplicación web (Java, Servlets y JSP) que **dibuja árboles genealógicos** y los guarda en **MySQL**. Este laboratorio es **individual** y aplica lo visto sobre **árboles**: usted construirá la estructura de datos, la lógica para armar un árbol desde texto y la persistencia.

La interfaz (páginas, menú y el dibujo en `canvas`) y los servlets **ya están hechos**. Usted completa **cinco archivos**.

### El árbol genealógico

Hay un **sujeto** (la persona de la que trata el árbol) en la posición `1`, con dos clases de familiares:

- **Ascendientes** (padres, abuelos...): sistema **Ahnentafel**. El padre de la persona `n` es `2n` y la madre es `2n + 1`.
  `2` = padre del sujeto, `3` = madre, `4` = padre del `2`, `5` = madre del `2`, `6` = padre del `3`... Es un **árbol binario**.
- **Descendientes** (hijos, nietos...): sistema **d'Aboville**. `1.1` es el primer hijo del sujeto, `1.2` el segundo, `1.1.1` el primer hijo del `1.1`... Es un **árbol general** (cada persona tiene una lista de hijos).

Solo existen los miembros que se escriben: no hay "huecos". Puede faltar el `3` aunque exista el `2`, o el `1.1` aunque exista el `1.2`.

### El texto de entrada

Cada línea es una persona: `posicion;nombre;año de nacimiento;año de fallecimiento` (el fallecimiento puede quedar vacío).

```
1;John Doe;2002;
2;Henry Doe;1970;2010
3;Maria Doe;1972;
4;Peter Doe;1940;
1.1;Timmy Doe;2021;
1.2;Amy Doe;2026;
1.1.1;Baby Doe;2045;
```

En la aplicación: **Construir árbol genealógico** → escribir el texto → **Construir árbol genealógico** (vista previa) → **Guardar en base de datos**. En **Árboles guardados** se abre un árbol y se pueden agregar padres, madres e hijos o eliminar miembros.

### Reglas

- La posición `1` (el sujeto) siempre existe y **no se puede eliminar**.
- Una posición no se repite. Un ascendiente solo se agrega si existe su hijo, y **debe haber nacido antes** que él. Un descendiente solo se agrega si existe su progenitor, y **debe haber nacido después**.
- Una persona válida tiene nombre (máx. 100 caracteres, sin `;`), nacimiento mayor que 0 y, si tiene fallecimiento, **no menor** que el nacimiento.
- **Eliminar** un ascendiente elimina también todos sus ascendientes; eliminar un descendiente elimina todos sus descendientes. Las demás posiciones **no cambian** de número.

## Qué debe implementar

| Archivo | ¿Se modifica? |
|---|---|
| `modelo/Posicion.java` | **Sí.** Clasificar y navegar posiciones (Etapa 1.1 y 1.2). |
| `modelo/Persona.java` | **Sí.** Método `validar()` (Etapa 1.3). |
| `modelo/ArbolGenealogico.java` | **Sí.** El árbol con **sus propios nodos** (Etapa 2). **No se permite usar `Map` ni `Set`**: las pruebas lo revisan. |
| `construccion/ConstructorArbol.java` | **Sí.** Texto → árbol (Etapa 3). |
| `dao/ArbolDAO.java` | **Sí.** Persistencia con JDBC (Etapa 4). |
| `src/test/java/...` | **No.** Son las pruebas. |
| `.github/` | **No.** Es la automatización. |
| Todo lo demás | No es necesario modificarlo. |

(Las rutas de los archivos están en `src/main/java/co/umariana/`.)

| Etapa | Qué se implementa | Pruebas |
|---|---|---|
| 1 | Posiciones (`Posicion`) y validaciones (`Persona`) | `PosicionTest`, `PersonaTest` (**modelos**) |
| 2 | `ArbolGenealogico`: agregar, buscar, eliminar y recorrer | `ArbolGenealogicoTest` |
| 3 | `ConstructorArbol`: crear el árbol desde el texto, con errores por línea | `ConstructorArbolTest` (**creación**) |
| 4 | `ArbolDAO`: guardar, listar, cargar, actualizar y eliminar | `ArbolDAOTest` (**persistencia**) |

Resuelva en orden: cada etapa usa las anteriores. Lea los comentarios de cada archivo: contienen las reglas exactas.

## Ejecutar el proyecto

Igual que en el laboratorio 9, con los scripts de la carpeta `scripts/` desde una terminal dentro de la carpeta del proyecto (en Windows use **Git Bash**).

| Acción | Comando |
|---|---|
| Ejecutar las pruebas | `./scripts/test.sh` |
| Crear la base de datos | `./scripts/crear-bd.sh` |
| Iniciar la aplicación | `./scripts/start.sh` |

### 1. Ejecutar las pruebas

Las pruebas **no necesitan MySQL** (usan una base de datos en memoria).

```bash
./scripts/test.sh
```

Al principio casi todas fallan; su trabajo es hacerlas pasar. Para ejecutar una sola etapa o clase:

```bash
./scripts/test.sh -Dtest='PosicionTest$Etapa1_Clasificar'
./scripts/test.sh -Dtest='ArbolGenealogicoTest'
```

### 2. Configurar la conexión a MySQL

1. Copie `.env.example` a `.env` y complete **todos** los valores (por ejemplo `DB_HOST=localhost`, `DB_PORT=3306`, `DB_NAME=arboles`, `DB_USER=root` y su contraseña):

   ```bash
   cp .env.example .env
   ```

2. Cree la base de datos y las tablas (una sola vez; el esquema está en `db/schema.sql`):

   ```bash
   ./scripts/crear-bd.sh
   ```

### 3. Ejecutar la aplicación completa

```bash
./scripts/start.sh
```

Abra http://localhost:8080/ cuando el texto deje de avanzar. Para detenerla: `Ctrl + C`. Si cambia el código, deténgala y vuelva a iniciarla.

Mientras una etapa no esté resuelta, la página correspondiente muestra el mensaje "Todavía falta implementar...". Pruebe la aplicación con el botón **Cargar un ejemplo**.

## Instrucciones generales

Estas instrucciones aplican para todos los ejercicios en este curso, se recomienda aprender estos pasos.

## 1. Clonar el repositorio

Copie la URL de **su** repositorio (botón verde **Code** en GitHub) y ejecute:

```bash
git clone <URL-DE-SU-REPOSITORIO>
```

Luego abra el repositorio desde VSCode (o su editor de preferencia).

## 2. Crear una rama nueva

**No trabaje sobre `main`.** Cree una rama para su ejercicio. 

```bash
git checkout -b LAB-10-juan-perez
```

## 3. Hacer cambios, probarlos y guardarlos (commit)

1. Edite los archivos de las etapas (ver la tabla de arriba).
2. Ejecute las pruebas **en su computador** antes de subir los cambios (`./scripts/test.sh`). También puede ejecutarlas en la nube al abrir el Pull Request (sección 5).
3. Revise qué cambió:
   ```bash
   git status
   ```
4. Agregue y guarde los cambios con un mensaje claro. El mensaje debe estar en infinitivo y responder a la pregunta: "¿Qué hizo?"

   ```bash
   git add src/main/java
   git commit -m "Implementar los árboles genealógicos"
   ```

5. Puede hacer varios commits pequeños a medida que resuelve cada ejercicio, o puede hacer un solo commit con la solución de todos los ejercicios, eso depende de usted.

## 4. Subir la rama (push)

```bash
git push origin LAB-10-juan-perez
```

## 5. Abrir el Pull Request (PR)

Una vez subida la rama, abra un Pull Request hacia `main`.
**El paso a paso de cómo abrirlo se explica en clase.**

El PR debe cumplir este formato (una acción automática lo verifica y marca ❌ si falta algo):

- **Título:** empieza con `LAB-N`, donde N es el número de la práctica. Ejemplo: `LAB-10: Árboles genealógicos`.
- **Descripción:** incluye una línea `Nombre: Su Nombre Completo`.

Si el formato falla, edite el título o la descripción del PR (botón **Edit**) y la validación se ejecuta de nuevo sola.

Cuando el PR esté abierto, para ejecutar las pruebas en GitHub:

1. En la columna derecha del PR, abra **Labels** y agregue **`Running Tests`**.
2. Espere a que termine (1–2 minutos). Aparecerá una etiqueta con el resultado:
   - 🟢 **`Test Passing`**: todas las pruebas pasan.
   - 🔴 **`Broken Test suite`**: alguna prueba falla o el proyecto no compila. Abra la pestaña **Checks → Pruebas** para ver el error.
3. Si falló, tiene la opción de corregir y subir nuevos cambios en otro commit.

> Las pruebas solo se ejecutan cuando usted agrega la etiqueta `Running Tests`.

---

## Evaluación

Nota de 0.0 a 5.0. Este ejercicio evalúa tanto el código como **el flujo de trabajo con Git y GitHub**.

| # | Criterio | Peso |
|---|---|---|
| 1 | Pruebas aprobadas | 40% |
| 2 | Código | 20% |
| 3 | Commits | 20% |
| 4 | Pull Request | 10% |
| 5 | Puntualidad | 10% |


