package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static cane.brothers.gpt.bot.telegram.commands.ReplyCallbackReplyCommand.NAME;

@Slf4j
@Component(NAME)
@RequiredArgsConstructor
class ReplyCallbackReplyCommand implements ChatCommand<CallbackQuery> {
    public static final String NAME = "/callback_reply";

    private final ChatSettings botSettings;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();

        var useCommand = botSettings.updateCommand(chatId, NAME);
        log.debug("Chat: %d. Change settings. Reply=%b".formatted(chatId, useCommand));

//        // return related command message
//        reply.set(command.getMessage(useCommand));
    }
}
