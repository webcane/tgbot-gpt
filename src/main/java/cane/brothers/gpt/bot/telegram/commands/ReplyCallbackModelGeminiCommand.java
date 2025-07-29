package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import cane.brothers.gpt.bot.telegram.settings.GptModelSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

import static cane.brothers.gpt.bot.telegram.commands.ReplyCallbackModelGeminiCommand.NAME;

@Slf4j
@Component(NAME)
@RequiredArgsConstructor
class ReplyCallbackModelGeminiCommand implements ChatCommand<CallbackQuery>, Utils {
    public final static String NAME = "/callback_model_gemini";
    private final static GptModel model = GptModel.GEMINI;

    private final ChatSettings botSettings;
    private final TelegramClient telegramClient;
    private final ChatCallbackCommandFactory callbackFactory;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();
        Integer messageId = data.getMessage().getMessageId();

        botSettings.setChatSetting(chatId, GptModelSetting.CALLBACK_MODEL, model);
        log.debug("Chat: %d. Change settings. set GPT Model=%s".formatted(chatId, model));

        if (messageId != null && botSettings.getUseReply(chatId)) {
            sendQuickReply(chatId, messageId, GptModelSetting.CALLBACK_MODEL.getMessage(model));
        }

        // hide callback menu
        ReplyCallbackHideSettingsCommand cmd = new ReplyCallbackHideSettingsCommand(callbackFactory, telegramClient);
        cmd.execute(data);
    }

    private void sendQuickReply(Long chatId, Integer messageId, String answer) throws TelegramApiException {
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