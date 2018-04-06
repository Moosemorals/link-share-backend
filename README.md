# Link Share Backend

I want to be able to share links with my husband without going through
some third party.

## Thinking about data

Links
    url             // Long string, 4k probably
    created         // timestamp
    owner           // Foreign key to users

Users
    name            // Long string
    id              // string

Passwords
    owner           // Foreign key to users
    algorithm       // String
    salt            // String
    hash            // String
    iterations      // int
    lastChanged     // timestamp
    locked          // boolean

Tokens
    owner
    token
    created
    lastUsed

Devices
    owner
    name

Settings
    owner
    name        // Probably not actually name, but a pointer to a name table
    value

LinkUser
    user
    visted
