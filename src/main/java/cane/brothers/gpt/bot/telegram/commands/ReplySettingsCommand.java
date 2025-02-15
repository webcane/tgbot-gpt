package cane.brothers.gpt.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component("/settings")
// TODO combine with ReplySettingsCallbackCommand
@RequiredArgsConstructor
class ReplySettingsCommand implements ChatCommand<Message>, KeyboardMarkup {

    private final TelegramClient telegramClient;

    @Override
    public void execute(Message data) throws TelegramApiException {
        var chatId = data.getChatId();
        log.debug("Chat: %d. Show settings menu".formatted(chatId));

        var reply = SendMessage.builder().chatId(chatId)
                .parseMode(ParseMode.MARKDOWNV2)
                .text("*Settings:*")
                .replyMarkup(getSettingsKeyboard())
                .build();

        telegramClient.execute(reply);
    }
}
