package cane.brothers.gpt.bot.ai;

import cane.brothers.gpt.bot.telegram.settings.GptModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatClientSelectorService {

    @Qualifier("openAiChatClient")
    private final ChatClient openAiChatClient;

    @Qualifier("geminiChatClient")
    private final ChatClient geminiChatClient;

    @Qualifier("deepSeekChatClient")
    private final ChatClient deepSeekChatClient;

    public ChatClient getClientByModel(GptModel model) {
        return switch (model) {
            case OPENAI -> openAiChatClient;
            case GEMINI -> geminiChatClient;
            case DEEPSEEK -> deepSeekChatClient;
            default -> throw new IllegalArgumentException("Unknown model: " + model);
        };
    }
}

