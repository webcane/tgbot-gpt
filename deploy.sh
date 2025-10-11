#!/bin/bash

#APP_DIR="${APP_DIR:-/home/ubuntu/app}"
APP_DIR="$(pwd)"
APP_NAME=tgbot-gpt
LOG_FILE="/var/log/$APP_NAME.log"


ENV_FILE="$APP_DIR/.env"
SSM_PARAMETER_ENV_FILE="/$APP_NAME/dot_env"

echo "Create .env file" >> " $LOG_FILE"
> "$ENV_FILE"

echo "Retrieving .env from SSM Parameter Store..." >> " $LOG_FILE"
aws ssm get-parameter --name "$SSM_PARAMETER_ENV_FILE" --with-decryption --query Parameter.Value --output text > "$ENV_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve .env from SSM. Check IAM permissions and parameter name."
    exit 1
fi
# file owner is ubuntu
sudo chown -R ubuntu:ubuntu "$ENV_FILE"
# only owner can read/write
sudo chmod 600 "$ENV_FILE"



DOCKER_COMPOSE_FILE="$APP_DIR/docker-compose.yml"
SSM_PARAMETER_DOCKER_COMPOSE_FILE="/$APP_NAME/docker-compose_yml"

echo "Create docker-compose.yml file" >> " $LOG_FILE"
> "$DOCKER_COMPOSE_FILE"

echo "Retrieving docker-compose.yml from SSM Parameter Store..." >> " $LOG_FILE"
aws ssm get-parameter --name "$SSM_PARAMETER_DOCKER_COMPOSE_FILE" --with-decryption --query Parameter.Value --output text > "$DOCKER_COMPOSE_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve docker-compose.yml from SSM. Check IAM permissions and parameter name."
    exit 1
fi
# file owner is ubuntu
sudo chown -R ubuntu:ubuntu "$DOCKER_COMPOSE_FILE"


echo "--- $(date) ---"
echo "Starting deployment..."

echo "Application directory: $APP_DIR"
cd "$APP_DIR" || { echo "Error: Cannot change directory to $APP_DIR. Exiting."; exit 1; }

echo "Stopping existing Docker Compose services..."
sudo -u ubuntu docker compose down -v --remove-orphans || true
if [ $? -ne 0 ]; then
    echo "Warning: docker compose down encountered issues, but continuing. Check logs if needed."
fi
echo "Existing services stopped."

echo "Starting new Docker Compose services..."
sudo -u ubuntu docker compose up -d --force-recreate
if [ $? -ne 0 ]; then
    echo "Error: Failed to start Docker Compose services. Exiting."
    exit 1
fi
echo "Application deployed and running."

echo "Deployment script finished successfully."