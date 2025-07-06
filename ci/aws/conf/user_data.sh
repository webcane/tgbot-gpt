#!/bin/bash

echo "Hello from user_data" >> /var/log/tgbot-gpt.log

# update
apt update
apt list --installed

# Install Docker
#apt install -y docker.io
#systemctl start docker
#systemctl enable docker

# install git

# install java
sudo apt install openjdk-21-jre-headless