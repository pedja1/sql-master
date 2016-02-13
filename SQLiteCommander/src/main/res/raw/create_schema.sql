CREATE TABLE IF NOT EXISTS _database (
	id TEXT PRIMARY KEY,
	type TEXT NOT NULL,
	database_uri TEXT NOT NULL,
	database_username TEXT DEFAULT NULL,
	database_password TEXT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS favorite (
	database_id TEXT
);

CREATE TABLE IF NOT EXISTS history (
	database_id TEXT
);

CREATE TABLE IF NOT EXISTS query_history (
	query TEXT
);

CREATE TABLE IF NOT EXISTS setting (
	key TEXT PRIMARY KEY,
	value TEXT
);