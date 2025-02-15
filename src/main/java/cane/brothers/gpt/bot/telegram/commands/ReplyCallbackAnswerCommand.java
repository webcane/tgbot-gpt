package cane.brothers.gpt.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component("/callback_answer")
@RequiredArgsConstructor
class ReplyCallbackAnswerCommand implements ChatCommand<CallbackQuery> {

    private final TelegramClient telegramClient;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var reply = AnswerCallbackQuery.builder().callbackQueryId(data.getId())
                .cacheTime(600) // 10min
                .build();
        telegramClient.execute(reply);
    }
}
