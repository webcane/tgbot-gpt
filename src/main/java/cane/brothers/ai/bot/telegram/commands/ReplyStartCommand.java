package cane.brothers.ai.bot.telegram.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component("/start")
@RequiredArgsConstructor
class ReplyStartCommand implements ChatCommand<Message> {

    @Override
    public void execute(Message data) throws TelegramApiException {
        // TODO reset AI dialog
    }
}
