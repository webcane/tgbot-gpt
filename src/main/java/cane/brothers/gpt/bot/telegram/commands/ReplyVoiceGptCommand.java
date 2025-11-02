package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.AppProperties;
import cane.brothers.gpt.bot.ai.ChatClientService;
import cane.brothers.gpt.bot.ai.ChatVoiceClientService;
import cane.brothers.gpt.bot.telegram.settings.ChatSettingsQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
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


@Slf4j
@Component("/voice-gpt")
class ReplyVoiceGptCommand extends ReplyGptCommand {

    //    private final static int TG_ANSWER_LIMIT = 4000 - 20;
    final ChatVoiceClientService voiceClient;
    final AppProperties properties;

    public ReplyVoiceGptCommand(ChatClientService chatClient,
                                TelegramClient telegramClient,
                                ChatSettingsQuery botSettings,
                                ChatVoiceClientService voiceClient,
                                AppProperties properties,
                                ConversionService convSvc) {
        super(chatClient, telegramClient, botSettings, convSvc);
        this.voiceClient = voiceClient;
        this.properties = properties;
    }

    @Override
    String getQuestion(Message data) throws TelegramApiException {
        // download voice
        GetFile getFileMethod = new GetFile(data.getVoice().getFileId());
        var file = telegramClient.execute(getFileMethod);
        log.debug(file.toString());

        var voiceFileResource = downloadFileResource(file);

        // voice to text
        return voiceClient.transcribe(voiceFileResource);
    }

    Resource downloadFileResource(File file) {
        String fileUrl = file.getFileUrl(properties.token());
        log.debug("remote voice file url: {}", fileUrl);
        Path downloadDir = Paths.get("src/main/resources");

        try {
            // create full file path to save
            var filePath = downloadDir.resolve(file.getFilePath());
            log.debug("save voice file to: {}", filePath);

            // Ensure the directories exist
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
                log.info("Created directories: {}", filePath.getParent());
            }

            // Ensure the file exists
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                log.info("Created file: {}", filePath);
            }

            // save received file from telegram
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
            throw new RuntimeException("unable to download file %s".formatted(file), ex);
        }
    }
}
