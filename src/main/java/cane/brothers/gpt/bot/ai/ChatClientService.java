package cane.brothers.gpt.bot.ai;

import cane.brothers.gpt.bot.telegram.settings.ChatSettingsQuery;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatClientService {

    @Qualifier("openAiChatClient")
    private final ChatClient openAiChatClient;

    @Qualifier("geminiChatClient")
    private final ChatClient geminiChatClient;

    private final ChatSettingsQuery botSettings;

    public String call(Long chatId, String userMessage) {
        GptModel model = botSettings.getGptModel(chatId);
        try {

            ChatClient client;
            switch (model) {
                case GEMINI -> client = geminiChatClient;
                case OPENAI -> client = openAiChatClient;
                default -> throw new IllegalArgumentException("Unknown model: " + model);
            }
            return client.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("AI error", ex);
            return "An error occurred while processing the request to %s.".formatted(model);
        }
    }
}
