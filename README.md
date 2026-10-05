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


## Prerequisites
- Docker Desktop (Docker Compose v2)
- OpenSSL
- Git

## Installation
1. Clone the repository:

   git clone https://github.com/Julls-cmd/secure-multitier-deploy.git
   cd secure-multitier-deploy

2. Generate the self-signed TLS certificates (use Git Bash or WSL on Windows):

   cd nginx
   chmod +x gen-certs.sh
   ./gen-certs.sh localhost
   cd ..

3. Build and start the containers:

   docker compose build
   docker compose up -d
   docker compose ps

4. Open https://localhost/ and accept the self-signed certificate warning.

## Available endpoints
| Resource       | URL                          |
|----------------|------------------------------|
| Application    | https://localhost/           |
| Backend health | https://localhost/api/health |
| Items API      | https://localhost/api/items  |

## Code documentation
Generate the backend Javadoc (Windows CMD, from the `backend` folder):

docker run --rm -v "%cd%":/app -w /app maven:3.9-eclipse-temurin-21 mvn clean javadoc:javadoc

The HTML site is generated in `backend/target/site/apidocs/index.html`.

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).