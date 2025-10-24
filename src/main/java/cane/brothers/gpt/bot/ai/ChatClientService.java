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
     * @param chatId      telegram chat Id
     * @param userMessage userMessage user prompt
     * @param userName    userName uses to provide extra details in case of error
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
            String detailedMessage = "";
            if (isAdminUser(userName)) {
                detailedMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            }
            return TgAnswer.builder()
                    .addPart("An error occurred while processing the request to %s.\n".formatted(model))
                    .addPart("**>", false) // The expandable block quotation
                    .addPart(detailedMessage)
                    .build();
        }
    }

    String getSystemMessage(Long chatId) {
        String systemMessage = "" ;
        if (botSettings.getUseMarkup(chatId)) {
            systemMessage = "Your entire response must be compatible with Telegram's markdown V2 formatting rules." ;
            // Use markdown V2 syntax for bold, italics, underline, strikethrough, inline code, code blocks, links, lists, and blockquotes.
            // When providing code snippets, always use triple backticks (```) to denote code blocks and specify the programming language if possible.
            // Avoid using unsupported HTML tags or attributes.
            // Escape all special characters as per Telegram's markdown V2 requirements. "
            log.debug("system message used. \"{}\"", systemMessage);
        }
        return systemMessage;
    }

    private boolean isAdminUser(String userName) {
        return properties.allowedUserNames() != null && !properties.allowedUserNames().isEmpty()
                && properties.allowedUserNames().contains(userName);
    }
}
