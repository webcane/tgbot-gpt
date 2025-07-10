#!/bin/bash

echo "Hello from user_data" >> /var/log/tgbot-gpt.log

# update
apt update
apt list --installed

# docker compose prereq
# sudo apt install -y gnome-terminal

# Add Docker's official GPG key:
#sudo apt-get update
#sudo apt-get install ca-certificates curl
#sudo install -m 0755 -d /etc/apt/keyrings
#sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
#sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
#echo \
#  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
#  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
#  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
#sudo apt-get update

# Install Docker compose
# sudo apt  install -y docker-compose
#apt install -y docker.io
#systemctl start docker
#systemctl enable docker

# install git

# install java
# sudo apt install openjdk-21-jre-headless