#!/bin/bash
echo "Starting user-data script execution..." >> "/var/log/${app_name}.log"

# init working repo
mkdir "/home/ubuntu/${app_name}.www"
chown -R ubuntu:ubuntu "/home/ubuntu/${app_name}.www"

# add Docker's official GPG key
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# add the repository to Apt sources
echo "deb [arch=${arch} signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${codename} stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

# install docker
echo "Install Docker" >> "/var/log/${app_name}.log"
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
docker --version
docker compose version

# add ubuntu user into docker group
sudo usermod -aG docker ubuntu
newgrp docker

# add .env data
echo "Create .env file" >> "/var/log/${app_name}.log"
touch "/home/ubuntu/${app_name}.www/.env"
sudo chown -R ubuntu:ubuntu "/home/ubuntu/${app_name}.www/.env"
echo "${env_data}" > "/home/ubuntu/${app_name}.www/.env"

# make deploy.sh executable
chmod +x "/home/ubuntu/${app_name}.www/deploy.sh"

# install aws cli
echo "Install jq" >> "/var/log/${app_name}.log"
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt update && sudo apt install -y unzip
unzip awscliv2.zip
sudo ./aws/install
/usr/local/bin/aws --version

# install jq
echo "Install jq" >> "/var/log/${app_name}.log"
sudo apt install -y jq

# Install and configure ECR Credential Helper

# Создаем директорию .docker, если ее нет
mkdir -p "${docker_config_dir}"
chmod 700 "${docker_config_dir}" # Устанавливаем безопасные права

echo "Install ECR Credential Helper" >> "/var/log/${app_name}.log"
# 1. Скачиваем и устанавливаем ECR Credential Helper, если его нет
if [ ! -f "${ecr_helper_path}" ]; then
    echo "ECR credential helper not found. Downloading..."
    curl -L "https://github.com/awslabs/amazon-ecr-credential-helper/releases/download/v${helper_version}/docker-credential-ecr-login-linux-amd64-v${helper_version}" -o "${ecr_helper_path}"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download ECR credential helper. Exiting."
        exit 1
    fi
    chmod +x "${ecr_helper_path}" # Делаем исполняемым
    echo "ECR credential helper downloaded and installed to ${ecr_helper_path}."
else
    echo "ECR credential helper already exists at ${ecr_helper_path}."
fi

echo "Configuring ECR Credential Helper" >> "/var/log/${app_name}.log"

# 2. Настраиваем Docker config.json для использования ECR Credential Helper
# Создаем или модифицируем config.json, чтобы Docker использовал ecr-login для ECR
echo "Configuring Docker to use ECR credential helper in ${docker_config_file}..."
# Используем jq для безопасного добавления/изменения ключей
# .credHelpers."public.ecr.aws" - для публичного ECR
# .credHelpers."*.dkr.ecr.aws" - для всех регионов приватного ECR
jq_command='.credHelpers."public.ecr.aws" = "ecr-login" | .credHelpers."*.dkr.ecr.aws" = "ecr-login"'

# Проверяем, существует ли config.json
if [ -f "${docker_config_file}" ]; then
    jq "$jq_command" "${docker_config_file}" > "${docker_config_file}.tmp" && mv "${docker_config_file}.tmp" "${docker_config_file}"
else
    # Создаем новый config.json
    echo "{}" | jq "$jq_command" > "${docker_config_file}"
fi

# Устанавливаем безопасные права на файл
chmod 600 "${docker_config_file}"
echo "Docker config.json updated. Credentials will not be stored unencrypted." >> "/var/log/${app_name}.log"
echo "ECR setup complete. Docker will now use IAM Role for ECR authentication." >> "/var/log/${app_name}.log"

echo "User-data script finished." >> "/var/log/${app_name}.log"