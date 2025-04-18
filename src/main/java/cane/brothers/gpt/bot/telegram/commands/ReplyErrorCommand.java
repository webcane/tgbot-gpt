package cane.brothers.gpt.bot.telegram.commands;

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
    private ErrorCommandContext context;

    @Override
    public void execute(Message data) throws TelegramApiException {
        var chatId = data.getChatId();
        Integer messageId = data.isReply() ? data.getReplyToMessage().getMessageId() : data.getMessageId();

        log.error("Chat: %d. username: %s. firstname: %s".formatted(chatId, data.getFrom().getUserName(), data.getFrom().getFirstName()));
        var errorMessage = "An error occurred while processing the request";
        if (context != null) {
            errorMessage += ".\n%s".formatted(context.errorMessage());
        }
        var reply = SendMessage.builder().chatId(chatId)
                .replyToMessageId(messageId)
                .text(errorMessage)
                .build();
        telegramClient.execute(reply);
    }

    @Override
    public void setContext(CommandContext context) {
        if (context instanceof ErrorCommandContext ctx) {
            this.context = ctx;
        }
    }
}
