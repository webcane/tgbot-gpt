#!/bin/bash

LOG_FILE="/var/log/${app_name}.log"
echo "Starting user-data script execution..." >> "$LOG_FILE"

APP_DIR="/home/ubuntu/${app_name}.www"

# init working repo
mkdir "$APP_DIR"
chown -R ubuntu:ubuntu "$APP_DIR"

# create voice dir
mkdir "$APP_DIR/voice"
chown -R ubuntu:ubuntu "$APP_DIR/voice"
chmod -R 770 "$APP_DIR/voice"

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

# install aws cli
echo "Install aws cli" >> " $LOG_FILE"
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt update && sudo apt install -y unzip
unzip awscliv2.zip
sudo ./aws/install
/usr/local/bin/aws --version

# install jq
echo "Install jq" >> " $LOG_FILE"
sudo apt install -y jq

# Define Docker config directory. To avoid warning '/root/.docker/',
DOCKER_CONFIG_DIR="/home/ubuntu/.docker"
DOCKER_CONFIG_FILE="$DOCKER_CONFIG_DIR/config.json"

# Create .docker directory if missing
# Create config.json. Docker will use ecr-login to access ECR
echo "Create $DOCKER_CONFIG_FILE..." >> " $LOG_FILE"
mkdir -p "$DOCKER_CONFIG_DIR"
# only owner can read/write/execute
chmod 700 "$DOCKER_CONFIG_DIR"

echo "Install and configure ECR Credential Helper" >> " $LOG_FILE"

ECR_HELPER_PATH="/usr/local/bin/docker-credential-ecr-login"
# use specific version for stability. Check latest on GitHub releases
# see https://github.com/awslabs/amazon-ecr-credential-helper/releases
HELPER_VERSION="0.10.1"

# Download and install ECR Credential Helper if missing
if [ ! -f "$ECR_HELPER_PATH" ]; then
    echo "ECR credential helper not found. Downloading..."
    sudo curl -Lo "$ECR_HELPER_PATH" "https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/$HELPER_VERSION/linux-amd64/docker-credential-ecr-login"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download ECR credential helper. Exiting."
        exit 1
    fi
    # make executable
    sudo chmod +x "$ECR_HELPER_PATH"
    echo "ECR credential helper downloaded and installed to $ECR_HELPER_PATH."
else
    echo "ECR credential helper already exists at $ECR_HELPER_PATH."
fi

echo "Configuring ECR Credential Helper" >> " $LOG_FILE"

# Configure docker config.json to use with  ECR Credential Helper
echo "Configuring Docker to use ECR credential helper in $DOCKER_CONFIG_FILE..." >> " $LOG_FILE"

# use jq to add keys safety
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

# set secure permissions
chmod 600 "$DOCKER_CONFIG_FILE"
echo "Docker config.json updated. Credentials will not be stored unencrypted. \
      ECR setup complete. Docker will now use IAM Role for ECR authentication." >> " $LOG_FILE"

echo "Creating google credentials file" >> " $LOG_FILE"

# default Google ADC path
CREDENTIALS_DIR="/home/ubuntu/.config/google"
CREDENTIALS_FILE="$CREDENTIALS_DIR/google-credentials.json"

# SSM Parameters
SSM_PARAMETER_CREDENTIALS_FILE="/${app_name}/google_credentials_json"


echo "Creating credentials directory for ubuntu user: $CREDENTIALS_DIR" >> " $LOG_FILE"
mkdir -p "$CREDENTIALS_DIR"
echo "Changing ownership of $CREDENTIALS_DIR to ubuntu:ubuntu" >> " $LOG_FILE"
# user owner is ubuntu
sudo chown ubuntu:ubuntu "$CREDENTIALS_DIR"
# only owner can read/write/execute
sudo chmod 700 "$CREDENTIALS_DIR"

echo "Retrieving Google credentials from SSM Parameter Store..." >> " $LOG_FILE"
aws ssm get-parameter --name "$SSM_PARAMETER_CREDENTIALS_FILE" --with-decryption --query Parameter.Value --output text > "$CREDENTIALS_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve credentials from SSM. Check IAM permissions and parameter name."
    exit 1
fi

echo "Setting ownership and permissions for $CREDENTIALS_FILE to ubuntu:ubuntu" >> "$LOG_FILE"
# file owner is ubuntu
sudo chown ubuntu:ubuntu "$CREDENTIALS_FILE"
# only owner can read/write
sudo chmod 600 "$CREDENTIALS_FILE"

echo "Google credentials saved to $CREDENTIALS_FILE for user 'ubuntu'." >> " $LOG_FILE"


ENV_FILE="$APP_DIR/.env"
SSM_PARAMETER_ENV_FILE="/${app_name}/.env"

echo "Create .env file" >> " $LOG_FILE"
touch "$ENV_FILE"

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
SSM_PARAMETER_DOCKER_COMPOSE_FILE="/${app_name}/docker-compose.yml"

echo "Create docker-compose.yml file" >> " $LOG_FILE"
touch "$DOCKER_COMPOSE_FILE"

echo "Retrieving docker-compose.yml from SSM Parameter Store..." >> " $LOG_FILE"
aws ssm get-parameter --name "$SSM_PARAMETER_DOCKER_COMPOSE_FILE" --with-decryption --query Parameter.Value --output text > "$DOCKER_COMPOSE_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve docker-compose.yml from SSM. Check IAM permissions and parameter name."
    exit 1
fi
# file owner is ubuntu
sudo chown -R ubuntu:ubuntu "$DOCKER_COMPOSE_FILE"


DEPLOY_FILE="$APP_DIR/deploy.sh"
SSM_PARAMETER_DEPLOY_FILE="/${app_name}/deploy.sh"

echo "Create deploy.sh file" >> " $LOG_FILE"
touch "$DEPLOY_FILE"

echo "Retrieving deploy.sh from SSM Parameter Store..." >> " $LOG_FILE"
aws ssm get-parameter --name "$SSM_PARAMETER_DEPLOY_FILE" --with-decryption --query Parameter.Value --output text > "$DEPLOY_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to retrieve deploy.sh from SSM. Check IAM permissions and parameter name."
    exit 1
fi
# file owner is ubuntu
sudo chown -R ubuntu:ubuntu "$DEPLOY_FILE"
# make deploy.sh executable
chmod +x "$DEPLOY_FILE"


echo "User-data script finished." >> " $LOG_FILE"