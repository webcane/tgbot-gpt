#!/bin/bash

#APP_DIR="${APP_DIR:-/home/ubuntu/app}"
APP_DIR="$(pwd)"
APP_NAME=tgbot-gpt

LOG_FILE="/var/log/$APP_NAME-deploy.log"
sudo chown ubuntu:ubuntu "$LOG_FILE"
echo "$(date '+%Y-%m-%d %H:%M:%S') Starting deploy script execution" >> "$LOG_FILE"


ENV_FILE=".env"
echo "Create $ENV_FILE file" >> "$LOG_FILE"
> $ENV_FILE
sudo chown ubuntu:ubuntu $ENV_FILE
echo "Retrieving $ENV_FILE from SSM Parameter Store..." >> "$LOG_FILE"
SSM_PARAMETER_ENV_FILE="/$APP_NAME/dot_env"
aws ssm get-parameter --name "$SSM_PARAMETER_ENV_FILE" --with-decryption --query Parameter.Value --output text > "$ENV_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve $ENV_FILE from SSM. Check IAM permissions and parameter name." >> "$LOG_FILE"
    exit 1
fi


DOCKER_COMPOSE_FILE="docker-compose.yml"
echo "Create $DOCKER_COMPOSE_FILE file" >> "$LOG_FILE"
> $DOCKER_COMPOSE_FILE
sudo chown ubuntu:ubuntu $DOCKER_COMPOSE_FILE
echo "Retrieving $DOCKER_COMPOSE_FILE from SSM Parameter Store..." >> "$LOG_FILE"
SSM_PARAMETER_DOCKER_COMPOSE_FILE="/$APP_NAME/docker_compose_yml"
aws ssm get-parameter --name "$SSM_PARAMETER_DOCKER_COMPOSE_FILE" --with-decryption --query Parameter.Value --output text > "$DOCKER_COMPOSE_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve $DOCKER_COMPOSE_FILE from SSM. Check IAM permissions and parameter name." >> "$LOG_FILE"
    exit 1
fi


echo "--- $(date) ---"
echo "Starting deployment..." >> "$LOG_FILE"


echo "Application directory: $APP_DIR" >> "$LOG_FILE"
cd "$APP_DIR" || { echo "Error: Cannot change directory to $APP_DIR. Exiting." >> "$LOG_FILE"; exit 1; }

echo "Stopping existing Docker Compose services..." >> "$LOG_FILE"
sudo -u ubuntu docker compose down -v --remove-orphans || true
if [ $? -ne 0 ]; then
    echo "Warning: docker compose down encountered issues, but continuing. Check logs if needed." >> "$LOG_FILE"
fi
echo "Existing services stopped." >> "$LOG_FILE"

echo "Pull the latest image from ECR" >> "$LOG_FILE"
sudo -u ubuntu docker compose pull

echo "Starting new Docker Compose services..." >> "$LOG_FILE"
sudo -u ubuntu docker compose up -d --force-recreate
if [ $? -ne 0 ]; then
    echo "Error: Failed to start Docker Compose services. Exiting." >> "$LOG_FILE"
    exit 1
fi
echo "Application deployed and running." >> "$LOG_FILE"


echo "Deployment script finished successfully." >> "$LOG_FILE"