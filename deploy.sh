#!/bin/bash

#APP_DIR="${APP_DIR:-/home/ubuntu/app}"
APP_DIR="$(pwd)"

echo "--- $(date) ---"
echo "Starting deployment..."

echo "Application directory: $APP_DIR"
cd "$APP_DIR" || { echo "Error: Cannot change directory to $APP_DIR. Exiting."; exit 1; }

echo "Stopping existing Docker Compose services..."
sudo -u ubuntu docker compose down --remove-orphans || true
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