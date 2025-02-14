package cane.brothers.ai.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component("/error")
@RequiredArgsConstructor
public class ReplyErrorCommand implements ChatCommand<Message> {

    private final TelegramClient telegramClient;

    @Override
    public void execute(Message data) throws TelegramApiException {
        var chatId = data.getChatId();
        var errorMessage = "Chat: %d. An error occurred while processing the request: %s"
                .formatted(chatId, data.getText());
        var reply = SendMessage.builder().chatId(chatId)
                .text(errorMessage)
                .build();
        telegramClient.execute(reply);
    }
}
