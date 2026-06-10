# Documentación técnica — Despliegue multi-tier seguro con Docker Compose

Proyecto de despliegue de una aplicación web multicapa con Docker Compose aplicando seguridad,
mínimo privilegio y aislamiento de redes.

## 1. Arquitectura del proyecto

Cinco servicios distribuidos en dos redes. El único punto de entrada al exterior es el proxy Nginx.

```
                 Internet
                    |
            :80 / :443 (host)
                    |
            +---------------+
            |  nginx-proxy  |
            +---------------+
             /             \
     frontend_net        backend_net (internal: true)
        |                  |          |
   +----------+      +----------+  +------+
   | frontend |      | backend  |  |  db  |
   +----------+      +----------+  +------+
                                      |
                               (uploads vol)
                                      |
                               +----------+
                               |   sftp   |  :2222 (host)
                               +----------+
```

| Servicio    | Imagen base                        | Puerto al host | Redes                     |
|-------------|------------------------------------|----------------|---------------------------|
| nginx-proxy | nginx:alpine                       | 80, 443        | frontend_net, backend_net |
| frontend    | nginxinc/nginx-unprivileged:alpine | ninguno        | frontend_net              |
| backend     | eclipse-temurin:21-jre-alpine      | ninguno        | backend_net               |
| db          | mysql:8                            | ninguno        | backend_net               |
| sftp        | atmoz/sftp:alpine                  | 2222           | backend_net, sftp_net     |

`backend_net` tiene `internal: true`: los contenedores en esa red no tienen salida a Internet
ni son accesibles desde el host.

---

## 2. Manual de despliegue

### 2.1 Requisitos

- Docker Desktop (con Docker Compose v2)
- OpenSSL
- Git

### 2.2 Generar los certificados TLS

Los certificados están en el `.gitignore` y hay que generarlos antes del primer arranque:

```bash
cd nginx
chmod +x gen-certs.sh
./gen-certs.sh localhost
cd ..
```

Crea `nginx/certs/server.crt` y `nginx/certs/server.key` (RSA 2048, autofirmado, 365 días).

> En Windows recomiendo ejecutarlo desde WSL o Git Bash para evitar problemas con rutas de OpenSSL.

### 2.3 Configurar la contraseña de /admin

El archivo `nginx/.htpasswd` ya está en el repositorio. Para cambiar la contraseña:

```bash
htpasswd -B -c nginx/.htpasswd admin
```

### 2.4 Arrancar el proyecto

```bash
docker compose build
docker compose up -d
docker compose ps
```

Compose respeta las dependencias: primero la base de datos, luego el backend (espera el healthcheck),
por último el proxy.

### 2.5 URLs

| Recurso            | URL / Comando                    |
|--------------------|----------------------------------|
| Aplicación         | https://localhost/               |
| Health del backend | https://localhost/api/health     |
| Items de la API    | https://localhost/api/items      |
| Archivos subidos   | https://localhost/uploads/       |
| Panel admin        | https://localhost/admin          |
| Conexión SFTP      | sftp -P 2222 uploader@localhost  |

El navegador mostrará un aviso de certificado autofirmado que hay que aceptar manualmente.

### 2.6 Subir un archivo por SFTP

```bash
sftp -P 2222 uploader@localhost
> cd upload
> put archivo.pdf
> bye
```

El archivo queda en `https://localhost/uploads/archivo.pdf` sin tocar permisos manualmente (ver 4.c).

### 2.7 Parar el entorno

```bash
docker compose down        # para los contenedores
docker compose down -v     # para los contenedores y borra los volúmenes
```

---

## 3. Manual de administración y hardening

### 3.1 Medidas de seguridad aplicadas

**Un solo punto de entrada.** Solo el proxy expone puertos (80 y 443). Backend y base de datos
no tienen ningún puerto al host.

**Aislamiento de redes.** `backend_net` es `internal: true`: sus contenedores no salen a Internet
ni son accesibles desde el host. El proxy es el único miembro de las dos redes.

**TLS y redirección HTTP → HTTPS.** Cifrado con `ssl_protocols TLSv1.2 TLSv1.3`.
El tráfico del puerto 80 se redirige al 443 con un 301.

**Cabeceras de seguridad.** `server_tokens off` oculta la versión de Nginx. Añadí
`X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` y `Strict-Transport-Security`
para mitigar clickjacking y sniffing de MIME.

**Basic Auth + rate limiting en /admin.** La ruta requiere usuario y contraseña y tiene
`limit_req_zone` a 5 r/s con ráfaga de 10, lo que frena ataques de fuerza bruta.

**Contenedores sin root.** El frontend usa `nginx-unprivileged` (no root). El backend tiene
un usuario `app` no privilegiado en el Dockerfile.

**Multi-stage y Alpine.** Las imágenes finales no incluyen Maven, JDK completo ni Node,
solo el artefacto y su runtime. Menos paquetes, menos superficie de ataque.

**Healthchecks.** El backend comprueba `/api/health`. El proxy no arranca hasta que el
backend esté `healthy`.

### 3.2 Límites de recursos (cgroups)

| Servicio | CPUs | Memoria |
|----------|------|---------|
| backend  | 1.0  | 512M    |
| db       | 1.0  | 768M    |

Si un servicio supera su límite de memoria, el kernel lo termina (OOM) en lugar de afectar al host.

### 3.3 Rotación de logs

```yaml
logging:
  driver: json-file
  options:
    max-size: "10m"
    max-file: "3"
```

Máximo 30MB por contenedor (3 archivos × 10MB). Los logs no llenan el disco en operación prolongada.

### 3.4 Volúmenes persistentes

- `db_data`: datos de MySQL, sobrevive a recreaciones del contenedor.
- `uploads`: compartido entre SFTP (escritura) y Nginx (lectura).

---

## 4. Retos de pensamiento crítico

### 4.a El Bypass

**¿Por qué existe?**
Si el backend publica el puerto 8080 al host (`8080:8080`), un atacante puede conectarse
directamente a `http://ip:8080` saltándose el proxy por completo: sin TLS, sin Basic Auth,
sin rate limiting.

**¿Cómo lo corregí?**
El backend no publica ningún puerto y vive en `backend_net` con `internal: true`.
El único acceso posible es a través del proxy, que resuelve `backend:8080` por DNS interno de Docker.

**Aislamiento de red.**
Cada servicio tiene acceso solo a lo que necesita. El frontend no ve la base de datos.
La base de datos no sale a Internet. El host solo ve el proxy. Todo bloqueado por defecto,
solo se abre lo necesario.

---

### 4.b El secreto filtrado

Cometí por error `nginx/certs/server.key` en la rama `develop`. Lo resolví con `hotfix/leaked-secret`:

1. Generé certificados nuevos con `gen-certs.sh`.
2. Añadí `*.key` y `nginx/certs/server.key` al `.gitignore`.
3. Eliminé el archivo del tracking con `git rm --cached nginx/certs/server.key`.
4. Merge del hotfix a `main` y `develop`, tag `v0.1.1`.

**Borrar el archivo no lo elimina del historial.** `git rm` solo lo quita del próximo commit.
La clave sigue recuperable en commits anteriores con `git checkout <commit> -- nginx/certs/server.key`,
así que hay que considerarla comprometida desde que entró al repositorio.

**La solución real es rotar la clave.** Generar un par nuevo hace que la clave filtrada deje de tener
valor. `git filter-repo` puede limpiar el historial pero no sustituye a la rotación: cualquiera que
ya tenga un clon sigue teniendo la clave antigua.

---

### 4.c UID/GID: permisos sin chmod 777

**El problema.** Docker crea el volumen como `root:root` antes de que arranque SFTP.
El entrypoint de `atmoz/sftp` ve el directorio ya existente y se salta el `chown`,
dejando `upload/` con permisos de root. El usuario `uploader` no puede escribir.

**La solución.** El script `sftp/umask.sh` fuerza los permisos en cada arranque:

```bash
#!/bin/sh

# Chroot debe pertenecer a root (requisito de sshd)
chown root:root /home/uploader
chmod 755 /home/uploader

# Subdirectorio de subida para uploader (UID/GID 101)
chown 101:101 /home/uploader/upload
chmod 750 /home/uploader/upload
```

El usuario SFTP se crea con UID/GID `101`, igual que el worker de Nginx. Los archivos subidos
pertenecen a `101:101` y Nginx los lee como propietario. La umask `0022` asegura que los archivos
se creen con permisos `644` y los directorios `755`.

---

### 4.d Benchmarking

**Metodología.**

1. `docker compose up -d`, esperar a que todo esté `healthy`.
2. `docker stats` en una terminal para monitorizar en tiempo real.
3. Generar carga: recargas en `https://localhost/` y peticiones a `https://localhost/api/items`.

**Resultados.**

| Servicio    | CPU % (reposo) | CPU % (carga) | MEM uso (reposo) | MEM uso (carga) | Límite |
|-------------|----------------|---------------|------------------|-----------------|--------|
| nginx-proxy |                |               |                  |                 | —      |
| frontend    |                |               |                  |                 | —      |
| backend     |                |               |                  |                 | 512M   |
| db          |                |               |                  |                 | 768M   |
| sftp        |                |               |                  |                 | —      |

> Rellenar con los datos reales de `docker stats` antes de entregar.

**Justificación de los límites.**
512MB es suficiente para Spring Boot en pruebas (la JVM en reposo no supera 200-300MB).
768MB permite a MySQL mantener el buffer pool en memoria. Si algún servicio superara su límite
y el kernel lo matara por OOM, habría que ajustar el valor y documentarlo.

---

## 5. Git Flow Report

El proyecto sigue Git Flow con ramas `main`, `develop`, `feature/*`, `release/*` y `hotfix/*`.
Commits en inglés siguiendo Conventional Commits.

```bash
git log --graph --oneline --all --decorate
```

> Adjuntar captura de pantalla de la salida de este comando.
