package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static cane.brothers.gpt.bot.telegram.commands.ReplyCallbackMarkupCommand.NAME;

@Slf4j
@Component(NAME)
@RequiredArgsConstructor
class ReplyCallbackMarkupCommand implements ChatCommand<CallbackQuery> {

    public static final String NAME = "/callback_markup";

    private final ChatSettings botSettings;
    private final TelegramClient telegramClient;
    private final ChatCallbackCommandFactory callbackFactory;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();

        var useCommand = botSettings.updateCommand(chatId, NAME);
        log.debug("Chat: %d. Change settings. Markup=%b".formatted(chatId, useCommand));

//        // return related command message
//        reply.set(command.getMessage(useCommand));

        // hide callback menu
        ReplyCallbackHideSettingsCommand cmd = new ReplyCallbackHideSettingsCommand(callbackFactory, telegramClient);
        cmd.execute(data);
    }
}
