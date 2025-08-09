package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static cane.brothers.gpt.bot.telegram.commands.SettingsReplyCommand.NAME;


@Slf4j
@Component(NAME)
@RequiredArgsConstructor
class SettingsReplyCommand implements ChatCommand<Message> {

    public static final String NAME = "/reply";
    private final ChatSettings botSettings;

    @Override
    public void execute(Message data) throws TelegramApiException {
        Long chatId = data.getChatId();

        var useCommand = botSettings.updateCommand(chatId, NAME);
        log.debug("Chat: %d. Change settings. Reply=%b".formatted(chatId, useCommand));
    }
}
