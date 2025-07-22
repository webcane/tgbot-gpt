#!/bin/bash
echo "Hello from user_data" >> "/var/log/${app_name}.log"

# configure git
git config --system init.defaultBranch master
git config --system user.name "ec2"
git config --system user.email "${email}"

# init git repo
mkdir /home/ubuntu/${app_name}.git
mkdir /home/ubuntu/${app_name}.www
cd /home/ubuntu/${app_name}.git
git init --bare
chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.git
chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.www

# create commit hook
touch /home/ubuntu/${app_name}.git/hooks/post-receive
sudo chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.git/hooks/post-receive
chmod +x /home/ubuntu/${app_name}.git/hooks/post-receive
echo ${hook_data} > /home/ubuntu/${app_name}.git/hooks/post-receive

# add Docker's official GPG key:
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
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
docker --version
docker compose version

# add ubuntu user into docker group
sudo usermod -aG docker ubuntu

# install gcloud dependencies
sudo apt install apt-transport-https ca-certificates gnupg curl -y

# add the google cloud sdk to apt sources
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list

# import google cloud public key
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -

# install google cloud sdk
sudo apt update
sudo apt install google-cloud-sdk -y

# init google cloud sdk
gcloud config set project ${google_cloud_project_id}
gcloud version

# add .env data
touch /home/ubuntu/${app_name}.www/.env
sudo chown -R ubuntu:ubuntu /home/ubuntu/${app_name}.www/.env
echo ${env_data} > /home/ubuntu/${app_name}.www/.env
