package cane.brothers.gpt.bot.telegram;

import cane.brothers.gpt.bot.AppProperties;
import org.eclipse.jetty.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.jetty.JettyTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
class TgBotConfig {

    @Bean
    public TelegramClient telegramClient(HttpClient httpClient, AppProperties properties) {
        return new JettyTelegramClient(httpClient, properties.token());
    }

}
