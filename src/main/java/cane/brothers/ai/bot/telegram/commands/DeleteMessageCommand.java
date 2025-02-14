package cane.brothers.ai.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component("/delete")
@RequiredArgsConstructor
public class DeleteMessageCommand implements ChatCommand<MaybeInaccessibleMessage> {

    private final TelegramClient telegramClient;

    @Override
    public void execute(MaybeInaccessibleMessage data) throws TelegramApiException {
        var chatId = data.getChatId();

        var reply = DeleteMessage.builder().chatId(chatId)
                .messageId(data.getMessageId())
                .build();
        telegramClient.execute(reply);
    }
}
