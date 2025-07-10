#!/bin/bash
echo "Hello from user_data" >> "/var/log/${app_name}.log"

# configure git
git config --global init.defaultBranch master
git config --global user.name "ec2"
git config --global user.email "${email}"

# init git repo
mkdir /home/ubuntu/${app_name}.git
mkdir /home/ubuntu/${app_name}.www
cd /home/ubuntu/${app_name}.git
git init --bare
chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.git
chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.www

# install docker compose
sudo apt-get update
sudo apt-get install docker-compose-plugin
docker compose version