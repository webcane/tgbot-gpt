package cane.brothers.ai.bot.telegram;

import cane.brothers.ai.bot.AppProperties;
import cane.brothers.ai.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.ai.bot.telegram.commands.ChatCommandFactory;
import cane.brothers.ai.bot.telegram.commands.ReplyErrorCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
class TgBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final AppProperties properties;
    private final ChatCommandFactory commandFactory;
    private final ChatCallbackCommandFactory callbackFactory;

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }

    @Override
    public void consume(Update update) {
        // message
        if (update.hasMessage()) {
            var userMessage = update.getMessage();

            try {
                var command = commandFactory.create(userMessage.getText());
                command.execute(userMessage);
            } catch (TelegramApiException tex) {
                log.error("Can't send message to telegram", tex);
                try {
                    var command = commandFactory.create(ReplyErrorCommand.class);
                    command.execute(userMessage);
                } catch (TelegramApiException ex) {
                    log.error("Can't send fallback message to telegram", ex);
                }
            } catch (Exception ex) {
                log.error("Exception occurred", ex);
            }
        }

        // callbacks
        else if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();

            try {
                var command = callbackFactory.create(callbackQuery.getData());
                command.execute(callbackQuery);
            } catch (TelegramApiException tex) {
                log.error("Can't send message to telegram", tex);
                try {
                    var command = callbackFactory.create("/callback_error");
                    command.execute(callbackQuery);
                } catch (TelegramApiException ex) {
                    log.error("Can't send fallback callback to telegram", ex);
                }
            } catch (Exception ex) {
                log.error("Exception occurred", ex);
            }
        }

        // unknown update
        else {
            log.warn("Unknown update. %s".formatted(update));
        }
    }
}
