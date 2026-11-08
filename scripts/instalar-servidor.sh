#!/usr/bin/env bash
# Instala y levanta Guali Dev en un servidor Ubuntu (EC2 / Lightsail).
# Uso (desde la carpeta del repositorio):  bash scripts/instalar-servidor.sh
set -euo pipefail

cd "$(dirname "$0")/.."

echo "==> 1/4 Memoria swap de 2 GB (ayuda a compilar con Maven en una instancia de 2 GB)"
if ! sudo swapon --show | grep -q '/swapfile'; then
  sudo fallocate -l 2G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab > /dev/null
fi

echo "==> 2/4 Docker"
if ! command -v docker > /dev/null; then
  curl -fsSL https://get.docker.com | sudo sh
  sudo usermod -aG docker "$USER"
fi

echo "==> 3/4 Archivo .env con contraseñas aleatorias"
if [ ! -f .env ]; then
  gen() { openssl rand -hex 16; }
  ADMIN_PW="Guali-$(openssl rand -hex 5)"
  cat > .env <<EOF
DB_NAME=guali_db
DB_USER=guali
DB_PASSWORD=$(gen)
DB_ROOT_PASSWORD=$(gen)
GUALI_TOKEN_INTERNO=$(gen)$(gen)
GUALI_ADMIN_USUARIO=admin
GUALI_ADMIN_PASSWORD=${ADMIN_PW}
EOF
  chmod 600 .env
fi

echo "==> 4/4 Construyendo y levantando los contenedores (la primera vez tarda 5-10 minutos)"
sudo docker compose up -d --build

echo
sudo docker compose ps
echo
echo "Listo. Usuario administrador: admin"
echo "Contraseña del administrador: $(grep GUALI_ADMIN_PASSWORD .env | cut -d= -f2)"
echo "(queda guardada en el archivo .env; cámbiala luego desde Administración > Usuarios)"
