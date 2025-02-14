package cane.brothers.ai.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component("/callback_settings")
@RequiredArgsConstructor
class ReplyCallbackSettingsCommand implements ChatCommand<CallbackQuery>, KeyboardMarkup {

    private final TelegramClient telegramClient;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();
        log.debug("Chat: %d. Show callback settings menu".formatted(chatId));

        // TODO commands chain
        ChatCommand<CallbackQuery> callbackAnswer = new ReplyCallbackAnswerCommand(telegramClient);
        callbackAnswer.execute(data);

        var reply = EditMessageText.builder().chatId(chatId)
                .messageId(data.getMessage().getMessageId())
                .parseMode(ParseMode.MARKDOWNV2)
                .text("*Settings:*")
                .replyMarkup(getSettingsKeyboard())
                .build();

        telegramClient.execute(reply);
    }
}
