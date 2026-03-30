#!/bin/bash
# Ejecutar localmente para buildear y desplegar en EC2
# Uso: bash deploy.sh <EC2_HOST> <PATH_KEY_PEM>
# Ejemplo: bash deploy.sh ec2-12-34-56-78.compute.amazonaws.com ~/keys/mi-clave.pem

set -e

EC2_HOST=$1
KEY_PEM=$2
EC2_USER="ec2-user"
REMOTE_DIR="/home/ec2-user/products"

if [ -z "$EC2_HOST" ] || [ -z "$KEY_PEM" ]; then
  echo "Uso: bash deploy.sh <EC2_HOST> <PATH_KEY_PEM>"
  exit 1
fi

echo "==> Compilando proyecto..."
mvn clean package -DskipTests

echo "==> Creando directorio remoto..."
ssh -i "$KEY_PEM" "$EC2_USER@$EC2_HOST" "mkdir -p $REMOTE_DIR/target"

echo "==> Copiando archivos a la EC2..."
scp -i "$KEY_PEM" Dockerfile "$EC2_USER@$EC2_HOST:$REMOTE_DIR/"
scp -i "$KEY_PEM" docker-compose.yml "$EC2_USER@$EC2_HOST:$REMOTE_DIR/"
scp -i "$KEY_PEM" products.sql "$EC2_USER@$EC2_HOST:$REMOTE_DIR/"
scp -i "$KEY_PEM" target/products-0.0.1-SNAPSHOT.jar "$EC2_USER@$EC2_HOST:$REMOTE_DIR/target/"

echo "==> Levantando contenedores..."
ssh -i "$KEY_PEM" "$EC2_USER@$EC2_HOST" "
  cd $REMOTE_DIR &&
  docker-compose down &&
  docker-compose up --build -d
"

echo ""
echo "Deploy completado. App disponible en: http://$EC2_HOST:8080"
