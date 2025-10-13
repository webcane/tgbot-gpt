package cane.brothers.gpt.bot.ai;

import cane.brothers.gpt.bot.telegram.settings.ChatSettingsQuery;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatClientService {

    private final ChatSettingsQuery botSettings;
    private final ChatClientSelectorService chatClientSelectorService;

    public String call(Long chatId, String userMessage) {
        GptModel model = botSettings.getGptModel(chatId);
        try {
            ChatClient client = chatClientSelectorService.getClientByModel(model);

            return client.prompt()
                    .user(userMessage)
                    .system(getSystemMessage(chatId))
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("AI error", ex);
            return "An error occurred while processing the request to %s.".formatted(model);
        }
    }

    String getSystemMessage(Long chatId) {
        String systemMessage = "";
        if (botSettings.getUseMarkup(chatId)) {
            systemMessage = """
                    Formats all responses using Telegram-compatible HTML. When including source code, always wrap it\s
in <pre><code>...</code></pre> tags to ensure proper display in Telegram messages. Avoid using Markdown syntax for\s
code blocks. Use HTML tags like <b>, <i>, <u>, and ......</a> for emphasis, links, and styling. Ensure all formatting\s
is valid and compatible with Telegram's HTML parse_mode.
""";
            log.debug("system message used. \"{}\"", systemMessage);
        }
        return systemMessage;
    }
}
