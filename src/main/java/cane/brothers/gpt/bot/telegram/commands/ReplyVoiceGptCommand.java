package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.AppProperties;
import cane.brothers.gpt.bot.openai.OpenAiVoiceService;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component("/voice")
@RequiredArgsConstructor
class ReplyVoiceGptCommand implements ChatCommand<Message>, Utils {

    private final static int TG_ANSWER_LIMIT = 4000 - 20;
    private final ChatClient chatClient;
    private final OpenAiVoiceService voiceClient;
    private final TelegramClient telegramClient;
    private final ChatSettings botSettings;
    private final AppProperties properties;


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

        // download voice
        String tmpDir = System.getProperty("java.io.tmpdir");
        log.debug("tmp dir: %s".formatted(tmpDir));

        var voiceFileName = tmpDir + "/voice.oga";
        log.debug("voice file name: %s".formatted(voiceFileName));

        GetFile getFileMethod = new GetFile(data.getVoice().getFileId());
        var file = telegramClient.execute(getFileMethod);
        log.debug(file.toString());

        if (downloadFile(file, voiceFileName)) {
            // voice to text
            var voicePrompt = voiceClient.transcribe(new FileSystemResource(voiceFileName));
            String answer = getGptAnswer(voicePrompt);

            // delete quick reply
            var delCommand = new DeleteMessageCommand(telegramClient);
            delCommand.execute(replyMessage);

            if (answer.length() > TG_ANSWER_LIMIT) {
                sendReplyFragments(chatId, messageId, answer, TG_ANSWER_LIMIT);
            } else {
                sendReply(chatId, messageId, answer);
            }
        }
    }

    private void sendReply(Long chatId, Integer messageId, String answer) throws TelegramApiException {
        var msgBuilder = SendMessage.builder().chatId(chatId);

        if (messageId != null && botSettings.getUseReply(chatId)) {
            // send reply message
            msgBuilder.replyToMessageId(messageId);
        }

        if (botSettings.getUseMarkup(chatId)) {
            msgBuilder.parseMode(ParseMode.MARKDOWNV2)
                    .text(escape(answer));
        } else {
            msgBuilder.text(Optional.ofNullable(answer).orElse("no clue"));
        }

        var reply = msgBuilder.build();
        telegramClient.execute(reply);
    }

    private void sendReplyFragments(Long chatId, Integer messageId, String answer, int maxLength) throws TelegramApiException {
        boolean sentReply = false;
        Pattern p = Pattern.compile("\\G\\s*(.{1," + maxLength + "})(?=\\s|$)", Pattern.DOTALL);
        Matcher m = p.matcher(answer);
        while (m.find()) {
            String fragment = m.group(1);
            sendReply(chatId, sentReply ? null : messageId, fragment);
            sentReply = true;
        }
    }

    boolean downloadFile(File file, String outputFileName) {
        String fileUrl = file.getFileUrl(properties.token());
        log.debug("remote voice file url: %s".formatted(fileUrl));

        try {
            java.io.File localFile = new java.io.File(outputFileName);
            InputStream is = new URL(fileUrl).openStream();
            FileUtils.copyInputStreamToFile(is, localFile);
            return true;
        } catch (IOException ex) {
            log.error("unable to download file %s".formatted(file), ex);
        }
        return false;
    }

    String getGptAnswer(String userMessage) {
        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("open-ai error", ex);
            return "An error occurred while processing the request to the open-ai service.";
        }
    }

    private void logUserMessage(Message data) {
        String user_first_name = data.getChat().getFirstName();
        String user_last_name = data.getChat().getLastName();
        String user_username = data.getChat().getUserName();
        long user_id = data.getChat().getId();
        log.info("username={} firstname={} lastname={} id={}", user_username, user_first_name, user_last_name, user_id);
    }

}
