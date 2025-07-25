#!/bin/bash
echo "Starting user-data script execution..." >> "/var/log/${app_name}.log"

# init working repo
mkdir /home/ubuntu/${app_name}.www
chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.www

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

# add .env data
echo "Create .env file" >> "/var/log/${app_name}.log"
touch /home/ubuntu/${app_name}.www/.env
sudo chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.www/.env
echo ${env_data} > /home/ubuntu/${app_name}.www/.env

echo "User-data script finished." >> "/var/log/${app_name}.log"
