#!/bin/bash

LOG_FILE="/var/log/${app_name}.log"
echo "Starting user-data script execution..." >> "$LOG_FILE"

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
echo "Install Docker" >> " $LOG_FILE"
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
docker --version
docker compose version

# add ubuntu user into docker group
sudo usermod -aG docker ubuntu
newgrp docker

# install gcloud dependencies
sudo apt install apt-transport-https ca-certificates gnupg curl -y

# add the google cloud sdk to apt sources
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list

# import google cloud public key
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -

# install google cloud sdk
echo "Install gcloud" >> " $LOG_FILE"
sudo apt update
sudo apt install google-cloud-sdk -y

# init google cloud sdk
echo "Configure google cloud sdk for user 'ubuntu'" >> " $LOG_FILE"
sudo -u ubuntu gcloud config set project ${google_cloud_project_id} >> "$LOG_FILE" 2>&1
gcloud version
if [ $? -ne 0 ]; then
    echo "Error: Failed to set gcloud project for user 'ubuntu'." >> "$LOG_FILE"
    exit 1
fi
echo "gcloud project set to ${google_cloud_project_id} for user 'ubuntu'." >> "$LOG_FILE"


# add .env data
echo "Create .env file" >> " $LOG_FILE"
touch "/home/ubuntu/${app_name}.www/.env"
sudo chown -R ubuntu:ubuntu "/home/ubuntu/${app_name}.www/.env"
echo "${env_data}" > "/home/ubuntu/${app_name}.www/.env"

# make deploy.sh executable
chmod +x "/home/ubuntu/${app_name}.www/deploy.sh"

# install aws cli
echo "Install jq" >> " $LOG_FILE"
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt update && sudo apt install -y unzip
unzip awscliv2.zip
sudo ./aws/install
/usr/local/bin/aws --version

# install jq
echo "Install jq" >> " $LOG_FILE"
sudo apt install -y jq

# Install and configure ECR Credential Helper

# Создаем директорию .docker, если ее нет
# Создаем или модифицируем config.json, чтобы Docker использовал ecr-login для ECR
echo "Create ${docker_config_file}..." >> " $LOG_FILE"
mkdir -p "${docker_config_dir}"
chmod 700 "${docker_config_dir}" # Устанавливаем безопасные права


echo "Install ECR Credential Helper" >> " $LOG_FILE"
# 1. Скачиваем и устанавливаем ECR Credential Helper, если его нет
if [ ! -f "${ecr_helper_path}" ]; then
    echo "ECR credential helper not found. Downloading..."
    sudo curl -Lo "${ecr_helper_path}" "https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/${helper_version}/linux-amd64/docker-credential-ecr-login"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download ECR credential helper. Exiting."
        exit 1
    fi
    sudo chmod +x "${ecr_helper_path}" # Делаем исполняемым
    echo "ECR credential helper downloaded and installed to ${ecr_helper_path}."
else
    echo "ECR credential helper already exists at ${ecr_helper_path}."
fi

echo "Configuring ECR Credential Helper" >> " $LOG_FILE"

# 2. Настраиваем Docker config.json для использования ECR Credential Helper
echo "Configuring Docker to use ECR credential helper in ${docker_config_file}..." >> " $LOG_FILE"
#sudo echo '{"credsStore":"ecr-login"}' > "${docker_config_dir}/config.json"

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

# login to ECR to store auth
# echo "login to ECR" >> " $LOG_FILE"
# aws ecr get-login-password | docker login --username AWS --password-stdin "${registry_prefix}"

# Устанавливаем безопасные права на файл
chmod 600 "${docker_config_file}"
echo "Docker config.json updated. Credentials will not be stored unencrypted. \
      ECR setup complete. Docker will now use IAM Role for ECR authentication." >> " $LOG_FILE"

echo "Creating google credentials file" >> " $LOG_FILE"

# Стандартное место для Google ADC
CREDENTIALS_DIR="/home/ubuntu/.config/google"
CREDENTIALS_FILE="$CREDENTIALS_DIR/google-credentials.json"
SSM_PARAMETER_NAME="/${app_name}/google_credentials_json"

echo "Creating credentials directory for ubuntu user: $CREDENTIALS_DIR" >> " $LOG_FILE"
mkdir -p "$CREDENTIALS_DIR"
echo "Changing ownership of $CREDENTIALS_DIR to ubuntu:ubuntu" >> " $LOG_FILE"
sudo chown ubuntu:ubuntu "$CREDENTIALS_DIR" # Передаем владение пользователю ubuntu
sudo chmod 700 "$CREDENTIALS_DIR" # Только владелец может читать/писать/исполнять

echo "Retrieving Google credentials from SSM Parameter Store..." >> " $LOG_FILE"
aws ssm get-parameter --name "$SSM_PARAMETER_NAME" --with-decryption --query Parameter.Value --output text > "$CREDENTIALS_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve credentials from SSM. Check IAM permissions and parameter name."
    exit 1
fi

echo "Setting ownership and permissions for $CREDENTIALS_FILE to ubuntu:ubuntu" >> "$LOG_FILE"
sudo chown ubuntu:ubuntu "$CREDENTIALS_FILE" # Передаем владение файлу
sudo chmod 600 "$CREDENTIALS_FILE" # Только владелец может читать/писать

echo "Google credentials saved to $CREDENTIALS_FILE for user 'ubuntu'." >> " $LOG_FILE"

# Настройте GOOGLE_APPLICATION_CREDENTIALS для вашего приложения
# echo "export GOOGLE_APPLICATION_CREDENTIALS=$CREDENTIALS_FILE" >> /etc/profile.d/google_credentials.sh
# chmod +x /etc/profile.d/google_credentials.sh
# echo "GOOGLE_APPLICATION_CREDENTIALS environment variable configured."

echo "Cloud-init script finished." >> " $LOG_FILE"