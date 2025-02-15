package cane.brothers.gpt.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component("/callback_error")
@RequiredArgsConstructor
class ReplyCallbackErrorCommand implements ChatCommand<CallbackQuery> {

    private final TelegramClient telegramClient;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();
        var errorMessage = "Chat: %d. An error occurred while processing the callback request.\n %s"
                .formatted(chatId, data.getData());
        var reply = SendMessage.builder().chatId(chatId)
                .text(errorMessage)
                .build();
        telegramClient.execute(reply);
    }
}
