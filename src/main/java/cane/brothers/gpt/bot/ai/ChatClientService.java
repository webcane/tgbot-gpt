package cane.brothers.gpt.bot.ai;

import cane.brothers.gpt.bot.AppProperties;
import cane.brothers.gpt.bot.telegram.TgAnswer;
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

    private final AppProperties properties;
    private final ChatSettingsQuery botSettings;
    private final ChatClientSelectorService chatClientSelectorService;

    /**
     * Get answer from AI model
     *
     * @param chatId telegram chat Id
     * @param userMessage userMessage user prompt
     * @param userName userName uses to provide extra details in case of error
     * @return TgAnswer
     */
    public TgAnswer call(Long chatId, String userMessage, String userName) {
        GptModel model = botSettings.getGptModel(chatId);
        try {
            ChatClient client = chatClientSelectorService.getClientByModel(model);
            var content = client.prompt()
                    .user(userMessage)
                    .system(getSystemMessage(chatId))
                    .call()
                    .content();
            return TgAnswer.builder().addPart(content).build();
        } catch (Exception ex) {
            log.error("AI error", ex);
            var detailedMessage = isAdminUser(userName) ? ex.getCause().getMessage(): "";
            return TgAnswer.builder()
                    .addPart("An error occurred while processing the request to %s.\n".formatted(model))
                    .addPart("**>", false) // The expandable block quotation
                    .addPart(detailedMessage)
                    .build();
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

    private boolean isAdminUser(String userName) {
        return properties.allowedUserNames() != null && !properties.allowedUserNames().isEmpty()
                && properties.allowedUserNames().contains(userName);
    }
}
