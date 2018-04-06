

-- Login

SELECT algorithm, salt, hash, iterations
    FROM passwords
    INNER JOIN users ON passwords.user = users.id
    WHERE locked = 0 AND name = ?;

-- Links shared with me

SELECT id, url, title, favIconURL, created, users.name, count
    FROM links
    INNER JOIN LinksUsers ON links.id = LinksUsers.link
    INNER JOIN users on LinksUsers.user = users.id;

-- Known users/devices

SELECT users.name, devices.name
    FROM users
    INNER JOIN devices ON users.id = devices.user;

-- Add a new link