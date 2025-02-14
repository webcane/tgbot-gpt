package cane.brothers.ai.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCallbackCommandFactory implements CommandFactory<CallbackQuery> {

    private final ApplicationContext context;

    @Override
    public ChatCommand<CallbackQuery> create(String message) {
        try {
            return (ChatCommand<CallbackQuery>) context.getBean(message);

        } catch (IllegalArgumentException | NestedRuntimeException ex) {
            log.debug(ex.getMessage());
            return (data) -> {
                var chatId = data.getMessage() == null? 0L : data.getMessage().getChatId();
                log.warn("Chat: %d. Unknown callback command: %s".formatted(chatId, data.getData()));
            };
        }
    }
}
