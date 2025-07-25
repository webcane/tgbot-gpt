package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.AppProperties;
import cane.brothers.gpt.bot.ai.ChatClientService;
import cane.brothers.gpt.bot.ai.ChatVoiceClientService;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component("/voice")
@RequiredArgsConstructor
class ReplyVoiceGptCommand implements ChatCommand<Message>, Utils {

    private final static int TG_ANSWER_LIMIT = 4000 - 20;
    private final ChatClientService chatClient;
    private final ChatVoiceClientService voiceClient;
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
        GetFile getFileMethod = new GetFile(data.getVoice().getFileId());
        var file = telegramClient.execute(getFileMethod);
        log.debug(file.toString());

        var voiceFileResource = downloadFileResource(file);
        if (voiceFileResource != null) {
            // voice to text
            var voicePrompt = voiceClient.transcribe(voiceFileResource);
            String answer = getGptAnswer(chatId, voicePrompt);

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

    Resource downloadFileResource(File file) {
        String fileUrl = file.getFileUrl(properties.token());
        log.debug("remote voice file url: {}", fileUrl);

//        String downloadDir = properties.voicePath();
        Path downloadDir = Paths.get("src/main/resources");

        try {
            // Создание полного пути для сохранения файла
            var filePath = downloadDir.resolve(file.getFilePath());
            log.debug("save voice file to: {}", filePath);

            // Убедитесь, что файл существует
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            // сохраняем файл
            URI uri = new URI(fileUrl);
            try (InputStream inputStream = uri.toURL().openStream()) {
                try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                    outputStream.getChannel().transferFrom(Channels.newChannel(inputStream), 0, Long.MAX_VALUE);
                }
            }

            var fp = filePath.toString();
            log.debug("relative path: {}", fp);
            return new FileSystemResource(filePath);
        } catch (URISyntaxException | IOException ex) {
            log.error("unable to download file {}", file, ex);
        }
        return null;
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
