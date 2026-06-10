#!/usr/bin/env bash
set -euo pipefail

CERT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/certs"
DAYS=365
CN="${1:-localhost}"

mkdir -p "$CERT_DIR"

openssl req -x509 -nodes \
    -newkey rsa:2048 \
    -keyout "$CERT_DIR/server.key" \
    -out "$CERT_DIR/server.crt" \
    -days "$DAYS" \
    -subj "/C=ES/ST=Madrid/L=Madrid/O=InfraDemo/CN=${CN}" \
    -addext "subjectAltName=DNS:${CN},DNS:localhost,IP:127.0.0.1"

chmod 600 "$CERT_DIR/server.key"
chmod 644 "$CERT_DIR/server.crt"

echo "Certificates written to ${CERT_DIR}"
