# Link Share Backend

I want to be able to share links with my husband without going through
some third party.

## Plan

This project has two parts. A Chrome extension that can send a link to the backend,
and a backend database that tracks links that have been shared (so we can share them
while the other is offline).

Backend database needs to track users and links. (even if there are only ever going to
be two users)

Connection to the server is through a Server Sent Events stream

On connect the client gives the ID of the last seen link

Client submits links with a POST

