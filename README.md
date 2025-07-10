# tgbot-gpt
This is a Telegram bot designed to serve as an GPT assistant. 
It was created using Java 17, utilizing the [Spring AI](https://spring.io/projects/spring-ai) framework and the [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots). The bot operates with the [GPT-4o](https://platform.openai.com/docs/models#gpt-4o) large language model from [OpenAI](https://platform.openai.com/docs/overview).

# Setup
1. Get your AI API key from [OpenAI API](https://openai.com/api)
2. Get your Telegram bot token from [@BotFather](https://t.me/BotFather)
3. Clone the repo to the server
    ```bash
    git clone https://github.com/webcane/tgbot-gpt.git
    ```
4. rename `.env.sample` to `.env` and specify required environmental variables there
    ```dotenv
    PROJECT=
    SERVER_PORT=
    SPRING_AI_OPENAI_API_KEY=
    TGBOT_TOKEN=
    TGBOT_VOICE_PATH=
    ```
5. To run the telegram bot over proxy define following env vars additionally:
    ```dotenv
    TGBOT_PROXY_HOSTNAME=
    TGBOT_PROXY_PORT=
    TGBOT_PROXY_USERNAME=
    TGBOT_PROXY_PASSWORD=
    ```
6. start the bot
    ```bash
    docker compose up --detach
    ```

# Development setup
TBD

# Notable features

# Message limits

# Telegram commands
```
/start - start new dialog
/markup - hightlight source code 
/reply - give a immediate response that the request is being processed
```

# Contributing

# Changelog

# Credits

