# Link Share Backend

I want to be able to share links with my husband without going through
some third party.

## Parts

### /backend

A Server Sent Events server that sends new links out to connected clients

### AuthManager

Track users and login state


Users have:
    name, password, hashed, tokens, machines

    tokens and machines are paired?

