package cane.brothers.gpt.bot.telegram.commands.model;

import cane.brothers.gpt.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.gpt.bot.telegram.commands.ChatCommand;
import cane.brothers.gpt.bot.telegram.commands.Utils;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import cane.brothers.gpt.bot.telegram.settings.GptModelSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
abstract class AbstractReplyCallbackModelCommand implements ChatCommand<CallbackQuery>, Utils {

    final ChatSettings botSettings;
    final TelegramClient telegramClient;
    final ChatCallbackCommandFactory callbackFactory;

    abstract GptModel getModel();

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();
        Integer messageId = data.getMessage().getMessageId();

        botSettings.setChatSetting(chatId, GptModelSetting.MODEL, getModel());
        log.debug("Chat: %d. Change settings. set GPT Model=%s".formatted(chatId, getModel()));

        if (messageId != null && botSettings.getUseReply(chatId)) {
            sendQuickReply(chatId, messageId, GptModelSetting.MODEL.getMessage(getModel()));
        }

        // hide callback menu
        ReplyCallbackHideSettingsCommand cmd = new ReplyCallbackHideSettingsCommand(callbackFactory, telegramClient);
        cmd.execute(data);
    }

    protected void sendQuickReply(Long chatId, Integer messageId, String answer) throws TelegramApiException {
        var msgBuilder = SendMessage.builder().chatId(chatId);

        // send reply message
        msgBuilder.replyToMessageId(messageId);

        if (botSettings.getUseMarkup(chatId)) {
            msgBuilder.parseMode(ParseMode.MARKDOWNV2)
                    .text(escape(answer));
        } else {
            msgBuilder.text(Optional.ofNullable(answer).orElse("no clue"));
        }

        var reply = msgBuilder.build();
        telegramClient.execute(reply);
    }
}
