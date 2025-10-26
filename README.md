# tgbot-gpt


This is a Telegram bot designed to serve as an GPT assistant.
It was created using Java 21, utilizing the [Spring AI](https://spring.io/projects/spring-ai) framework and
the [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots). The bot operates with

- [gpt-4.1-mini](https://platform.openai.com/docs/models/gpt-4.1-mini) large language model
  from [OpenAI](https://platform.openai.com/docs/overview)
- [gemini-2.0-flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash)
  from [Google AI](https://ai.google/)
- [DeepSeek-V3.2-Exp](https://api-docs.deepseek.com/quick_start/pricing) from [DeepSeek](https://deepseek.com/)

![Gradle Build](https://github.com/webcane/tgbot-gpt/workflows/Deploy/badge.svg)

# Features

- Multi-models

  Supports following optional AI models:
    - OpenAI GPT-4.1-Mini
    - Google Gemini-2.0-Flash
    - DeepSeek DeepSeek-V3.2-Exp

- Speech-to-text Support

  Voice messages are transcribed using OpenAI Whisper model

- Proxy Support

  The bot can be configured to run behind a proxy server for enhanced security and privacy

- User Access Control

  Only specified Telegram admin usernames are allowed to see detailed error messages

- Cloud-First Deployment Strategy

  1. Build in the Cloud
    - The Docker image is built using the GitHub Actions environment, ensuring consistency and scalability.
    - A fully automated CI/CD pipeline facilitates seamless building and deployment of the bot. 
    - To maintain a clean and efficient deployment process, avoid pulling source code or building the image directly on the EC2 instance.

  2. Simplified Deployment Process 
    - Deployment to the EC2 instance requires only two files:
      - docker-compose.yml 
      - deploy.sh 
    - The bot can be deployed effortlessly using Docker and Docker Compose, streamlining the setup and reducing manual intervention.
    - commit to `main` branch triggers github actions workflow to build, push docker image into AWS ECR repository and redeploy the bot on EC2 instance.

  3. Automated Infrastructure Provisioning 
    - The EC2 instance is configured automatically using a cloud-init script, enabling consistent and repeatable setups. 
    - Terraform scripts are provided to provision the AWS EC2 instance and configure essential infrastructure components, such as networking, security groups, and IAM roles.

- Env Variables Management

  Environment variables can be simply managed using AWS SSM Parameter Store for secure configuration

- Spring AI Framework

  - Built using the Spring AI framework for seamless integration with AI services
    - see [ChatClientSelectorService](./src/main/java/cane/brothers/gpt/bot/ai/ChatClientSelectorService.java)
  - Each bot command is implemented as a separate Spring component
  - Used factory design pattern to create command components
    - start with [CommandFactory](./src/main/java/cane/brothers/gpt/bot/telegram/commands/CommandFactory.java)
  - application [configuration](./src/main/java/cane/brothers/gpt/bot/AppProperties.java) is managed using Spring Boot properties
  - Supports easy extension and customization of bot functionality
  
- Telegram Bot

  - Utilizes the Telegram Bot Java Library for efficient interaction with the [Telegram Bot](./src/main/java/cane/brothers/gpt/bot/telegram/TgBot.java) API
  - Supports various Telegram entities:
    - [telegram bot commands](#Telegram-commands)
    - text messages
    - voice messages
    - callback queries
  - Utilizes [bot settings](./src/main/java/cane/brothers/gpt/bot/telegram/settings/CommandSetting.java) for configuration
  - Supports message replying and code formatting

# Prerequisites

- [Docker](https://docs.docker.com/get-docker/) installed
- [Docker Compose](https://docs.docker.com/compose/install/) installed
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) installed and configured
- [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) installed
- java 21 installed
- gradle installed
- [gcloud CLI](#google-api-key) installed and authenticated

# Setup

1. Get your Telegram bot token from [@BotFather](https://t.me/BotFather)
2. Get your AI API key from [OpenAI API](https://openai.com/api)
3. Get your [Google API key](#google-api-key)
4. Get your [DeppSeek API key](https://platform.deepseek.com/api_keys)
5. Register AWS account
6. Setup proxy (Optional)

## Google API key

1. Create Google Cloud Platform project
2. Define API key to call Generative Language API only
3. Install the [gcloud CLI](https://cloud.google.com/sdk/docs/install#deb) to use Google Gemini model
4. Authenticate by running following commands
   ```bash
   gcloud auth application-default login
   ```

# Configuration

Create a `.env` file in the root directory and add the following:

1. basic configuration
    ```dotenv
    PROJECT=<bot_name>
    SERVER_PORT=8080
    TGBOT_TOKEN=
    TGBOT_VOICE_PATH=
    TGBOT_ALLOWED_USER_NAMES=
    ```
2. Model configuration
- if you want to use OpenAI model, add the following 
    ```dotenv
    # Open AI
    OPENAI_API_KEY=
    ```
- if you want to use Gemini model, add the following 
    ```dotenv
    # Gemini
    GOOGLE_CLOUD_PROJECT_ID=
    GOOGLE_CLOUD_REGION=europe-west1
    ```
- if you want to use DeepSeek model, add the following 
    ```dotenv
    # DeepSeek
    DEEPSEEK_API_KEY=
    ```
3. To run the telegram bot over proxy define following env vars additionally:
    ```dotenv
    TGBOT_PROXY_HOSTNAME=
    TGBOT_PROXY_PORT=42567
    TGBOT_PROXY_USERNAME=
    TGBOT_PROXY_PASSWORD=
    ```

# Running the App

There are several ways to run the bot:

1. locally
2. locally in docker
3. or on AWS EC2 instance

## Local Setup

To run the bot locally:

- build the project
    ```bash
    gradlew bootJar
    ```
- run the bot from the command line
    ```bash
    et -a
    source .env
    set +a
    java -jar build/libs/app.jar
    ```
- Run from IDE
  Use the `App` run configuration with environment variables loaded from the .env file.
-

## Local Docker Setup

- build an image
    ```bash
    docker build -t "${PROJECT,,}:latest" . 
    ```
- run the bot using docker compose
  make sure `.env` file is in the same directory as `docker-compose.yml`
    ```bash
    docker compose up --detach
    ```
- to stop the bot
    ```bash
    docker compose down -v
    ```
- to see logs
    ```bash
    docker compose logs -f
    ```
- to clean up unused docker objects
    ```bash
    docker system prune -a
    ```

# EC2 setup
there are two steps to deploy the bot on AWS EC2 instance:
1. [setup github](#github-setup) workflow
2. [setup AWS infrastructure](#AWS-infrastructure-setup) using terraform scripts
3. [deploy](.github/workflows/deploy.yml) the bot using github actions workflow

## Github setup

github actions will build and push docker image into AWS ECR repository. Then it will redeploy the bot on EC2 instance.
To run `deploy` workflow, github needs to have access to AWS account:

- create `aws` environment in [settings](https://github.com/webcane/tgbot-gpt/settings/environments)
- define following environment variables in the `aws` environment
    - `AWS_ACCESS_KEY_ID` - aws access key id
    - `AWS_SECRET_ACCESS_KEY` - aws secret access key

## AWS infrastructure setup

The project has defined following terraform modules:

- [infra](#infra)
- [params](#params)

### infra

Creates ec2 instance with necessary security groups, iam roles, etc.

Use Terraform scripts to provision the required AWS resources.
Terraform will do following:

- create ec2 and ecr using terraform modules
- define free_tier alerts
- use `t3.small` ec2 instance type. note that is not free tier eligible
- setup security groups to allow only ssh and http access
- setup iam roles and policies:
  github actions will be allowed to:
    - push docker images into ECR
    - run AWS SSM command
    - write SSM command execution logs into CloudWatch logs
- it will be allowed to redeploy the bot using AWS SSM command
- use cloud-init script to:
    - setup and configure docker
    - setup aws-cli
    - setup and configure gcloud sdk
    - install and configure ecr credential helper
    - create working directory `/home/ubuntu/tgbot-gpt`
    - download `deploy.sh` script into working directory
    - write cloud-init logs into `/var/log/cloud-init-output.log`

> Keep in mind that `cloud-init` script will run only once when the instance is created.

To deploy the infrastructure:

> Create `tgbot-gpt-tf` S3 bucket in `eu-central-1` region to store terraform state
> or keep terraform state locally.

1. Init terraform script
    ```bash
    cd ./ci/aws/infra
    terraform init -reconfigure \
        -backend-config="bucket=tgbot-gpt-tf" \
        -backend-config="region=eu-central-1" \
        -backend-config="key=tgbot-gpt-infra.tfstate"
    ```
2. Provide terraform variables over `terraform.tfvars` or inline
3. Deploy dockerized application on EC2 instance by running terraform scripts
    ```bash
    terraform plan -out tgbot-gpt.tfplan
    terraform apply -input=false tgbot-gpt.tfplan
    ```
4. Keep `aws_ec2_id` terraform output value.

### params

If some env variables need to be updated, use `params` terraform module to upload env variables into AWS SSM Parameter
Store.

1. Init terraform script
    ```bash
    cd ./ci/aws/params
    terraform init -reconfigure \
    -backend-config="bucket=tgbot-gpt-tf" \
    -backend-config="region=eu-central-1" \
    -backend-config="key=tgbot-gpt-params.tfstate"
    ```
2. To upload env variables do the following:

- define `aws.env` file locally
- upload env variables into AWS SSM Parameter Store by running
    ```bash
    terraform plan -out tgbot-gpt-params.tfplan
    terraform apply -input=false tgbot-gpt-params.tfplan
    ```

3. Start/restart the application

   Start docker container manually by SSH or run deploy.sh script by the following aws command
    ```bash
    aws ssm send-command \
	  --document-name "AWS-RunShellScript" \
	  --parameters 'commands=["cd /home/ubuntu/tgbot-gpt", "./deploy.sh"]' \
	  --instance-ids "<ec2-instance-id>" \
	  --comment "Deploy tgbot-gpt" \
	  --cloud-watch-output-config "CloudWatchLogGroupName=/aws/ssm/tgbot-gpt-deploy-logs,CloudWatchOutputEnabled=true" \
	  --region "eu-central-1"
    ```

# Notable features

- The EC2 instance is configured with only a root volume. Each time Terraform provisions the instance,
  all data is lost and the environment is reinitialized using the `user_data` cloud-init script.
- Current setup
  uses [t3.small](https://eu-central-1.console.aws.amazon.com/ec2/home?region=eu-central-1#InstanceTypes:v=3;search=:t3.small)
  ec2 instance type (2GiB Memory, 2 vCPU). This is not free tier eligible
- 

# Message limits

# Telegram commands

```
reply - work in progress reply
markup - format gpt response
models - choose preferred ai model
```

# Project Structure

- `/.github` - github actions workflows
- `/.run` - run configurations for IDE
- `/ci` - continuous integration scripts
    - `/ci/aws/infra` - terraform scripts to provision AWS infrastructure
    - `/ci/aws/params` - terraform scripts to upload env variables into AWS SSM Parameter Store
- `/gradle` - gradle wrapper files
- `/src` - java source code
- `.env` - environment variables file
- `aws.env` - environment variables file for AWS SSM Parameter Store
- `docker-compose.yml` - docker compose file to run the bot locally
- `Dockerfile` - dockerfile to build the bot image
- `build.gradle` - gradle build file
- `gradle.properties` - gradle properties file

# Testing

- TBD

# Troubleshooting

- TBD

# Contributing

- TBD

# Changelog

- init project
- add openai gpt-4.1-mini model support
- add google gemini-2.0-flash model support
- add terraform scripts to provision AWS infrastructure
- add terraform scripts to upload env variables into AWS SSM Parameter Store
- add github actions workflow to deploy the bot on AWS EC2 instance
- add deepseek deepseek-v3.2-exp model support

# Credits

- Created by [webcane](https://github.com/webcane/tgbot-gpt)

# License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
