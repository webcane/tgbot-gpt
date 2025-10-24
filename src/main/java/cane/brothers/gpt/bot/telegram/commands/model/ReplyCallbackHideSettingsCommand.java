package cane.brothers.gpt.bot.telegram.commands.model;

import cane.brothers.gpt.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.gpt.bot.telegram.commands.ChatCommand;
import cane.brothers.gpt.bot.telegram.commands.DeleteMessageCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component("/callback_hide_settings")
@RequiredArgsConstructor
class ReplyCallbackHideSettingsCommand implements ChatCommand<CallbackQuery> {

    private final ChatCallbackCommandFactory callbackFactory;
    private final TelegramClient telegramClient;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();
        log.debug("Chat: %d. Hide settings".formatted(chatId));

        // TODO commands chain
//        ChatCommand<CallbackQuery> callbackAnswer = new ReplyCallbackAnswerCommand(getTelegramClient(), getBotSettings());
//        callbackAnswer.execute(data);
        callbackFactory.create("/callback_answer").execute(data);


//        messageFactory.create("/delete").execute(data.getMessage());
        var delCommand = new DeleteMessageCommand(telegramClient);
        delCommand.execute(data.getMessage());
    }
}
