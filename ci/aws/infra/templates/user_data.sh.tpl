#!/bin/bash

LOG_FILE="/var/log/${app_name}.log"
echo "Starting user-data script execution" >> "$LOG_FILE"

APP_DIR="/home/ubuntu/${app_name}.www"
echo "Create working directory $APP_DIR..." >> "$LOG_FILE"
mkdir "$APP_DIR"
chown -R ubuntu:ubuntu "$APP_DIR"

echo "Create voice directory $APP_DIR/voice..." >> "$LOG_FILE"
mkdir "$APP_DIR/voice"
sudo chown -R ubuntu:ubuntu "$APP_DIR/voice"
sudo chmod -R 770 "$APP_DIR/voice"

echo "Add Docker's official GPG key" >> "$LOG_FILE"
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo "Add the repository to Apt sources" >> "$LOG_FILE"
echo "deb [arch=${arch} signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${codename} stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

echo "Install Docker" >> "$LOG_FILE"
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
docker --version
docker compose version

echo "Add ubuntu user into docker group" >> "$LOG_FILE"
sudo usermod -aG docker ubuntu
newgrp docker

echo "Create .docker directory for ubuntu user if missing" >> "$LOG_FILE"
# Define Docker config directory for ubuntu user to avoid warning '/root/.docker/',
DOCKER_CONFIG_DIR="/home/ubuntu/.docker"
mkdir "$DOCKER_CONFIG_DIR"
sudo chown ubuntu:ubuntu "$DOCKER_CONFIG_DIR"

echo "Install gcloud dependencies" >> "$LOG_FILE"
sudo apt install apt-transport-https ca-certificates gnupg curl -y

echo "Add the google cloud sdk to apt sources" >> "$LOG_FILE"
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list

echo "Import google cloud public key" >> "$LOG_FILE"
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -

echo "Install gcloud sdk" >> "$LOG_FILE"
sudo apt update
sudo apt install google-cloud-sdk -y

echo "Configure google cloud sdk for user ubuntu" >> "$LOG_FILE"
sudo -u ubuntu gcloud config set project ${google_cloud_project_id} >> "$LOG_FILE" 2>&1
gcloud version
if [ $? -ne 0 ]; then
    echo "Error: Failed to set gcloud project for user ubuntu." >> "$LOG_FILE"
    exit 1
fi
echo "gcloud project set to ${google_cloud_project_id} for user ubuntu." >> "$LOG_FILE"

echo "Install aws cli" >> "$LOG_FILE"
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt update && sudo apt install -y unzip
unzip awscliv2.zip
sudo ./aws/install
/usr/local/bin/aws --version

echo "Install jq" >> "$LOG_FILE"
sudo apt install -y jq

echo "Install and configure ECR Credential Helper" >> "$LOG_FILE"
ECR_HELPER_PATH="/usr/local/bin/docker-credential-ecr-login"
# use specific version for stability. Check latest on GitHub releases
# see https://github.com/awslabs/amazon-ecr-credential-helper/releases
HELPER_VERSION="0.10.1"
# Download and install ECR Credential Helper if missing
if [ ! -f "$ECR_HELPER_PATH" ]; then
    echo "ECR credential helper not found. Downloading..." >> "$LOG_FILE"
    sudo curl -Lo "$ECR_HELPER_PATH" "https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/$HELPER_VERSION/linux-amd64/docker-credential-ecr-login"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download ECR credential helper. Exiting." >> "$LOG_FILE"
        exit 1
    fi
    # make executable
    sudo chmod +x "$ECR_HELPER_PATH"
    echo "ECR credential helper downloaded and installed to $ECR_HELPER_PATH." >> "$LOG_FILE"
else
    echo "ECR credential helper already exists at $ECR_HELPER_PATH." >> "$LOG_FILE"
fi

DOCKER_CONFIG_FILE="$DOCKER_CONFIG_DIR/config.json"
# Docker will use ecr-login to access ECR
# Configure docker config.json to use with ECR Credential Helper
echo "Configuring Docker to use ECR credential helper in $DOCKER_CONFIG_FILE..." >> "$LOG_FILE"
echo "Use jq to add keys safety" >> "$LOG_FILE"
# .credHelpers."public.ecr.aws" - for public  ECR
# .credHelpers."*.dkr.ecr.aws" - for all regions - private ECR
jq_command='.credHelpers."public.ecr.aws" = "ecr-login" | .credHelpers."*.dkr.ecr.aws" = "ecr-login"'
# Check if config.json exist
if [ -f "$DOCKER_CONFIG_FILE" ]; then
    jq "$jq_command" "$DOCKER_CONFIG_FILE" > "$DOCKER_CONFIG_FILE.tmp" && mv "$DOCKER_CONFIG_FILE.tmp" "$DOCKER_CONFIG_FILE"
else
    # Create new config.json with credsHelpers section
    echo "{}" | jq "$jq_command" > "$DOCKER_CONFIG_FILE"
fi

echo "Changing ownership of $DOCKER_CONFIG_FILE to ubuntu:ubuntu" >> "$LOG_FILE"
sudo chown ubuntu:ubuntu "$DOCKER_CONFIG_FILE"
if [ $? -ne 0 ]; then
    echo "Error: Failed to set ownership for $DOCKER_CONFIG_FILE." >> "$LOG_FILE"
    exit 1
fi
sudo chmod 600 "$DOCKER_CONFIG_FILE"
if [ $? -ne 0 ]; then
    echo "Error: Failed to set permissions for $DOCKER_CONFIG_FILE." >> "$LOG_FILE"
    exit 1
fi

echo "Docker config.json updated. Credentials will not be stored unencrypted. \
ECR setup complete. Docker will now use IAM Role for ECR authentication." >> "$LOG_FILE"

# default Google ADC path
CREDENTIALS_DIR="/home/ubuntu/.config/google"
echo "Creating credentials directory for ubuntu user: $CREDENTIALS_DIR" >> "$LOG_FILE"
mkdir -p "$CREDENTIALS_DIR"
echo "Changing ownership of $CREDENTIALS_DIR to ubuntu:ubuntu" >> "$LOG_FILE"
# user owner is ubuntu
sudo chown ubuntu:ubuntu "$CREDENTIALS_DIR"

CREDENTIALS_FILE="$CREDENTIALS_DIR/google-credentials.json"
echo "Create $CREDENTIALS_FILE file" >> "$LOG_FILE"
touch "$CREDENTIALS_FILE"
echo "Setting ownership and permissions for $CREDENTIALS_FILE to ubuntu:ubuntu" >> "$LOG_FILE"
sudo chown ubuntu:ubuntu "$CREDENTIALS_FILE"
sudo chmod 600 "$CREDENTIALS_FILE"

echo "Retrieving Google credentials from SSM Parameter Store..." >> "$LOG_FILE"
SSM_PARAMETER_CREDENTIALS_FILE="/${app_name}/google_credentials_json"
aws ssm get-parameter --name "$SSM_PARAMETER_CREDENTIALS_FILE" --with-decryption --query Parameter.Value --output text > "$CREDENTIALS_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve credentials from SSM. Check IAM permissions and parameter name." >> "$LOG_FILE"
    exit 1
fi
echo "Google credentials saved to $CREDENTIALS_FILE for user 'ubuntu'." >> "$LOG_FILE"

DEPLOY_FILE="$APP_DIR/deploy.sh"
echo "Create deploy.sh file" >> "$LOG_FILE"
touch "$DEPLOY_FILE"

echo "Retrieving deploy.sh from SSM Parameter Store..." >> "$LOG_FILE"
SSM_PARAMETER_DEPLOY_FILE="/${app_name}/deploy_sh"
aws ssm get-parameter --name "$SSM_PARAMETER_DEPLOY_FILE" --with-decryption --query Parameter.Value --output text > "$DEPLOY_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve deploy.sh from SSM. Check IAM permissions and parameter name." >> "$LOG_FILE"
    exit 1
fi

echo "Setting ownership and permissions for $DEPLOY_FILE to ubuntu" >> "$LOG_FILE"
sudo chown -R ubuntu:ubuntu "$DEPLOY_FILE"
sudo chmod 600 "$DEPLOY_FILE"
chmod +x "$DEPLOY_FILE"

echo "User-data script finished." >> "$LOG_FILE"