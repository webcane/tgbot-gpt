#!/bin/bash
while read oldrev newrev ref
do
    if [[ \$ref =~ .*/master\$ ]];
    then
        echo "Master ref received. Deploying master branch to production..."
        pwd
        git --work-tree=/home/ubuntu/${app_name}.www --git-dir=/home/ubuntu/${app_name}.git checkout -f
        cd ../${app_name}.www
        docker compose down
        docker compose up --detach
    else
        echo "Ref \$ref successfully received. Doing nothing: only the master branch may be deployed on this server."
    fi
done