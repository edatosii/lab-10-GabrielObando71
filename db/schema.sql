-- Esquema de la base de datos. Lo usan:
--   * MySQL (ejecútelo una vez en su base de datos; ver el README)
--   * las pruebas (H2 en memoria, modo MySQL)
-- Mantenga SQL estándar para que funcione en ambos.

-- Un árbol genealógico guardado (su nombre y cuándo se creó).
CREATE TABLE IF NOT EXISTS arboles (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    creado TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Los miembros de cada árbol. "posicion" es la posición Ahnentafel (2, 3, 4...) o d'Aboville (1.1, 1.2.1...).
-- Si se borra un árbol, sus miembros se borran solos (ON DELETE CASCADE).
CREATE TABLE IF NOT EXISTS miembros (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    arbol_id      INT         NOT NULL,
    posicion      VARCHAR(50) NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    nacimiento    INT         NOT NULL,
    fallecimiento INT         NULL,
    CONSTRAINT uq_miembro_posicion UNIQUE (arbol_id, posicion),
    CONSTRAINT fk_miembro_arbol FOREIGN KEY (arbol_id) REFERENCES arboles (id) ON DELETE CASCADE
);
