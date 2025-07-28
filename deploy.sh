#!/bin/bash

# Загружаем переменные из .env файла
set -a
source .env
set +a

# --- 1. Определение переменных ---
APP_DIR="/home/ubuntu/$PROJECT.www"
AWS_REGION=${AWS_REGION:-eu-central-1} # Установите дефолт или убедитесь, что переменная приходит
AWS_ACCOUNT=${AWS_ACCOUNT:-$(aws sts get-caller-identity --query Account --output text --region "$AWS_REGION")} # Автоматически получить ID аккаунта

echo "--- $(date) ---"
echo "Starting deployment..."
echo "Application directory: $APP_DIR"

cd "$APP_DIR" || { echo "Error: Cannot change directory to $APP_DIR. Exiting."; exit 1; }

# --- 2. Обновление .env файла (если используете) ---
# Этот блок остается важным для конфигурации приложения
#echo "Updating .env file..."
#{
#  echo "TELEGRAM_BOT_TOKEN=$(aws ssm get-parameter --name "/your-app/telegram_bot_token" --with-decryption --query Parameter.Value --output text --region YOUR_AWS_REGION)"
#  # ... другие переменные ...
#} > .env
#if [ ! -f .env ]; then
#    echo "Error: .env file was not created. Exiting."
#    exit 1
#fi
#echo ".env file updated successfully."

# --- 3. Аутентификация в ECR ---
# ЭТОТ ШАГ КРИТИЧЕСКИ ВАЖЕН! Docker Compose нужен доступ к ECR.
echo "Logging in to ECR using IAM Role..."
# Используем ECR_REGISTRY для логина, даже если не делаем pull явно
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com"
if [ $? -ne 0 ]; then
    echo "Error: Failed to log in to ECR. Exiting."
    exit 1
fi
echo "ECR login successful."

# --- 4. Остановка текущего Docker Compose приложения ---
echo "Stopping existing Docker Compose services..."
docker compose down --remove-orphans || true
if [ $? -ne 0 ]; then
    echo "Warning: docker compose down encountered issues, but continuing. Check logs if needed."
fi
echo "Existing services stopped."

# --- 5. Запуск приложения с Docker Compose ---
# Docker Compose сам потянет образ, если его нет или он устарел.
echo "Starting new Docker Compose services..."
# --force-recreate важен, чтобы пересоздать контейнер, даже если образ с тегом 'latest' обновился
# --pull (явное) или --always-pull (в Docker Compose v2.x) можно добавить для гарантированного pull,
# но по умолчанию Docker Compose и так проверяет и тянет новые образы с тегом 'latest'.
docker compose up -d --force-recreate # --always-pull
if [ $? -ne 0 ]; then
    echo "Error: Failed to start Docker Compose services. Exiting."
    exit 1
fi
echo "Application deployed and running."

echo "Deployment script finished successfully."