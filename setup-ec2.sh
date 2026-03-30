#!/bin/bash
# Ejecutar en la EC2 luego de conectarse por SSH
# sudo bash setup-ec2.sh

set -e

echo "==> Actualizando paquetes..."
yum update -y

echo "==> Instalando Docker..."
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user

echo "==> Instalando Docker Compose..."
COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep '"tag_name"' | cut -d'"' -f4)
curl -SL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

echo "==> Versiones instaladas:"
docker --version
docker-compose --version

echo ""
echo "IMPORTANTE: Cerrá y volvé a abrir la sesión SSH para que el grupo 'docker' tome efecto."
