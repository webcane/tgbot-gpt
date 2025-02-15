package cane.brothers.gpt.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCommandFactory implements CommandFactory<Message> {

    private final ApplicationContext context;

    @Override
    public ChatCommand<Message> create(String message) {
        try {
            if (message.startsWith("/")) { // registered command
                return (ChatCommand<Message>) context.getBean(message);
            }
            else { // gpt prompt
                return (ChatCommand<Message>) context.getBean("/gpt");
            }
        } catch (IllegalArgumentException | NestedRuntimeException ex) {
            log.debug(ex.getMessage());
            return (data) -> {
                var chatId = data.getChatId();
                log.warn("Chat: %d. Unknown message command: %s".formatted(chatId, data.getText()));
            };
        }
    }
}
