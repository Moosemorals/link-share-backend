
CREATE TABLE users (
    id CHAR(40) PRIMARY KEY,
    name VARCHAR(128)
);

CREATE TABLE links (
    id CHAR(40) PRIMARY KEY,
    url VARCHAR(4096),
    title VARCHAR(4096),
    favIconURL VARCHAR(4096),
    created TIMESTAMP,
    owner CHAR(40) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE LinksUsers (
    user CHAR(40) REFERENCES users (id) ON DELETE CASCADE,
    link CHAR(40) REFERENCES links (id) ON DELETE CASCADE,
    count INTEGER
);

CREATE TABLE passwords (
    user CHAR(40) REFERENCES users (id) ON DELETE CASCADE,
    algorithm VARCHAR(128),
    salt VARCHAR(128),
    hash VARCHAR(128),
    iterations INT,
    lastChanged TIMESTAMP,
    locked INT
);

CREATE TABLE devices (
    id CHAR(40) PRIMARY KEY,
    user CHAR(40) REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(4096)
);

CREATE TABLE tokens (
    user CHAR(40) REFERENCES users(id) ON DELETE CASCADE,
    device CHAR(40) REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(128),
    created TIMESTAMP,
    lastUsed TIMESTAMP
);

CREATE TABLE SettingsNames (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(128)
);

CREATE TABLE Settings (
    user CHAR(40) REFERENCES users(id) ON DELETE CASCADE,
    name BIGINT REFERENCES SettingsNames(id) ON DELETE CASCADE,
    value VARCHAR(4096)
);