CREATE TABLE IF NOT EXISTS _database (
  id                TEXT PRIMARY KEY,
  type              TEXT NOT NULL,
  database_uri      TEXT NOT NULL,
  database_name     TEXT NOT NULL,
  database_username TEXT    DEFAULT NULL,
  database_password TEXT    DEFAULT NULL,
  database_port     INTEGER DEFAULT 3360,
  is_favorite       INTEGER,
  created           INTEGER,
  accessed          INTEGER
);

CREATE TABLE IF NOT EXISTS query_history (
  query TEXT PRI
);

CREATE TABLE IF NOT EXISTS setting (
  key   TEXT PRIMARY KEY,
  value TEXT
);