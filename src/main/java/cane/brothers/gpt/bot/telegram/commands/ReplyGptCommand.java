package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.ai.ChatClientService;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import cane.brothers.gpt.bot.telegram.settings.ChatSettingsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component("/gpt")
@RequiredArgsConstructor
class ReplyGptCommand implements ChatCommand<Message>, Utils {

    private final static int TG_ANSWER_LIMIT = 4000 - 20;
    private final ChatClientService chatClient;
    private final TelegramClient telegramClient;
    private final ChatSettingsQuery botSettings;

    @Override
    public void execute(Message data) throws TelegramApiException {
        Long chatId = data.getChatId();
        Integer messageId = data.isReply() ? data.getReplyToMessage().getMessageId() : data.getMessageId();
        logUserMessage(data);

        // quick reply
        var reply = SendMessage.builder().chatId(chatId)
                .replyToMessageId(messageId)
                .text("already working on it...")
                .build();
        var replyMessage = telegramClient.execute(reply);

        String answer = getGptAnswer(chatId, data.getText());

        // delete quick reply
        var delCommand = new DeleteMessageCommand(telegramClient);
        delCommand.execute(replyMessage);

        if (answer.length() > TG_ANSWER_LIMIT) {
            sendReplyFragments(chatId, messageId, answer, TG_ANSWER_LIMIT);
        } else {
            sendReply(chatId, messageId, answer);
        }
    }

    private void sendReply(Long chatId, Integer messageId, String answer) throws TelegramApiException {
        var msgBuilder = SendMessage.builder().chatId(chatId);

        if (messageId != null && botSettings.getUseReply(chatId)) {
            // send reply message
            msgBuilder.replyToMessageId(messageId);
        }

        if (botSettings.getUseMarkup(chatId)) {
            //            var escapedText = escape(answer);
            var escapedText = answer;
            msgBuilder.parseMode(ParseMode.HTML)
                    .text(escapedText);
        } else {
            msgBuilder.text(Optional.ofNullable(answer).orElse("no clue"));
        }

        var reply = msgBuilder.build();
        telegramClient.execute(reply);
    }

    private void sendReplyFragments(Long chatId, Integer messageId, String answer, int maxLenght) throws TelegramApiException {
        boolean sentReply = false;
        Pattern p = Pattern.compile("\\G\\s*(.{1," + maxLenght + "})(?=\\s|$)", Pattern.DOTALL);
        Matcher m = p.matcher(answer);
        while (m.find()) {
            String fragment = m.group(1);
            sendReply(chatId, sentReply ? null : messageId, fragment);
            sentReply = true;
        }
    }

    String getGptAnswer(Long chatId, String userMessage) {
        return chatClient.call(chatId, userMessage);
    }

    private void logUserMessage(Message data) {
        String user_first_name = data.getChat().getFirstName();
        String user_last_name = data.getChat().getLastName();
        String user_username = data.getChat().getUserName();
        long user_id = data.getChat().getId();
        log.info("username={} firstname={} lastname={} id={}", user_username, user_first_name, user_last_name, user_id);
    }
}
