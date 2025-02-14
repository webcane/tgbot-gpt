package cane.brothers.ai.bot.telegram;

import cane.brothers.ai.bot.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
class TgBotConfig {

    @Bean
    public TelegramClient telegramClient(OkHttpClient okClient, AppProperties properties) {
        return new OkHttpTelegramClient(okClient, properties.token());
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotApplication(ObjectMapper objectMapper,
                                                                     OkHttpClient okClient) {
        return new TelegramBotsLongPollingApplication(() -> objectMapper, () -> okClient);
    }
}
