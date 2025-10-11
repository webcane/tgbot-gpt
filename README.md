# tgbot-gpt
This is a Telegram bot designed to serve as an GPT assistant. 
It was created using Java 21, utilizing the [Spring AI](https://spring.io/projects/spring-ai) framework and the [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots). The bot operates with 
 - [gpt-4.1-mini](https://platform.openai.com/docs/models/gpt-4.1-mini) large language model from [OpenAI](https://platform.openai.com/docs/overview)
 - [gemini-2.0-flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash) from [Google AI](https://ai.google/)

# Github actions build
![Gradle Build](https://github.com/webcane/tgbot-gpt/workflows/Build%20and%20Push%20to%20ECR/badge.svg)


# Setup

1. Get your Telegram bot token from [@BotFather](https://t.me/BotFather)
2. Get your AI API key from [OpenAI API](https://openai.com/api)
3. Get your [Google API key](#google-api-key)
4. Register AWS account
5. Setup proxy (Optional)


## Google API key

1. Create Google Cloud Platform project
2. Define API key to call Generative Language API only
3. Install the [gcloud CLI](https://cloud.google.com/sdk/docs/install#deb) to use Google Gimini model
4. Authenticate by running following commands
   ```bash
   gcloud auth application-default login
   ```

## Local setup

1. specify required environmental variables in `.env`
    ```dotenv
    PROJECT=<bot_name>
    SERVER_PORT=8080
    TGBOT_TOKEN=
    TGBOT_VOICE_PATH=
    TGBOT_ALLOWED_USER_NAMES=
    # Open AI
    OPENAI_API_KEY=
    # Gemini
    GOOGLE_CLOUD_PROJECT_ID=
    GOOGLE_CLOUD_REGION=europe-west1
    ```
2. To run the telegram bot over proxy define following env vars additionally:
    ```dotenv
    TGBOT_PROXY_HOSTNAME=
    TGBOT_PROXY_PORT=42567
    TGBOT_PROXY_USERNAME=
    TGBOT_PROXY_PASSWORD=
    ```
3. build an image
    ```bash
    docker build -t "${PROJECT,,}:latest" . 
    docker build --build-arg PORT=${SERVER_PORT} --build-arg DOCKER_USER_NAME=${DOCKER_USER_NAME} -t "${PROJECT,,}:latest" .
    ```
4. start the bot
    ```bash
    docker compose up --detach
    ```
   
## AWS setup
Use terraform scripts to provision required aws objects.
Terraform will do following:
- create ec2 and ecr using terraform modules
- define free_tier alerts
- github actions will be allowed to push docker images into ECR
- it will be allowed to redeploy the bot using AWS SSM command
- setup necessary software (docker, aws-cli, etc.)
- creates `.env` file

1. Init terraform script
    ```bash
    cd ./ci/aws
    terraform init -reconfigure \
        -backend-config="bucket=tgbot-gpt-tf" \
        -backend-config="region=eu-central-1" \
        -backend-config="key=tgbot-gpt.tfstate"
    ```
2. Provide terraform variables over `terraform.tfvars` or inline
3. Deploy dockerized application on EC2

    run terraform scripts
    ```bash
    terraform plan -out tgbot-gpt.tfplan
    terraform apply -input=false tgbot-gpt.tfplan
    ```
4. Keep `aws_ec2_id` terraform output value.
5. Start/restart the application
   
   Start docker container manually by SSH or run deploy.sh script by aws command
    ```bash
    aws ssm send-command \
	  --document-name "AWS-RunShellScript" \
	  --parameters 'commands=["cd /home/ubuntu/tgbot-gpt.www", "./deploy.sh"]' \
	  --instance-ids "<ec2-instance-id>" \
	  --comment "Deploy tgbot-gpt" \
	  --cloud-watch-output-config "CloudWatchLogGroupName=/aws/ssm/tgbot-gpt-deploy-logs,CloudWatchOutputEnabled=true" \
	  --region "eu-central-1"
    ```

# Development setup

## docker compose
## aws cli
### configure aws credentials
## terraform 

# Notable features
- The EC2 instance is configured with only a root volume. Each time Terraform provisions the instance, 
all data is lost and the environment is reinitialized using the `user_data` cloud-init script.

# Message limits

# Telegram commands
```
reply - work in progress reply
markup - format gpt response
models - choose preferred ai model
```

# Contributing

# Changelog

# Credits

