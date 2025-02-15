package cane.brothers.gpt.bot.telegram;

import cane.brothers.gpt.bot.AppProperties;
import cane.brothers.gpt.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.gpt.bot.telegram.commands.ChatCommandFactory;
import cane.brothers.gpt.bot.telegram.commands.ReplyErrorCommand;
import cane.brothers.gpt.bot.telegram.info.TgBotInfo;
import cane.brothers.gpt.bot.telegram.info.TgBotInfoFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TgBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final AppProperties properties;
    private final ChatCommandFactory commandFactory;
    private final ChatCallbackCommandFactory callbackFactory;
    private final TgBotInfoFetcher botInfo;
    private String botUsername;

    @Override
    public String getBotToken() {
        return properties.token();
    }


//    public String getBotUsername() {
//        return "your_bot_username";
//    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
        TgBotInfo bi = botInfo.getInfo();
        this.botUsername = bi.getBotUsername();
        log.debug(bi.toString());
    }

    @Override
    public void consume(Update update) {
        // message
        if (update.hasMessage()) {
            var userMessage = update.getMessage();

            try {
                // user chat message
                if (isUserChatMessage(userMessage)) {
                    var command = commandFactory.create(userMessage.getText());
                    command.execute(userMessage);
                }
                // group chat message - request to bot
                else if (isGroupChatMessageToBot(userMessage)) {
                    var prompt = userMessage.getText().substring(botUsername.length() + 1);

                    if(userMessage.isReply()) {
                        prompt += ". " + userMessage.getReplyToMessage().getText();
                    }
                    var command = commandFactory.create(prompt);
                    command.execute(userMessage);
                }
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

    boolean isUserChatMessage(Message userMessage) {
        var chat = userMessage.getChat();
        return chat != null && chat.isUserChat();
    }

    boolean isGroupChatMessageToBot(Message userMessage) {
        var chat = userMessage.getChat();
        if (chat != null && chat.isSuperGroupChat() && userMessage.hasEntities()) {
            for (var messageEntity : userMessage.getEntities()) {
                if (isMentionType("mention", messageEntity, botUsername)
                        || isMentionType("text_link", messageEntity, botUsername)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isMentionType(String type, MessageEntity messageEntity, String botName) {
        return type.equals(messageEntity.getType()) && messageEntity.getText().equals(botName);
    }
}
