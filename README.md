# Multi-tier deployment

Hardened multi-tier deployment built with Docker Compose: an Nginx reverse proxy
terminating TLS at the edge, a React frontend, a Spring Boot API, a MySQL database
reachable only from an internal network, and a chrooted SFTP upload gateway.

## Layout

```
backend/    Spring Boot service (Java 21)
frontend/   React app (Vite)
nginx/      Reverse proxy, TLS termination, hardening
db/         MySQL schema and seed data
sftp/       SFTP gateway configuration
```

See `DOCUMENTACION.md` for the deployment and administration manual.
