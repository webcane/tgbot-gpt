package cane.brothers.gpt.bot.telegram.commands.model;

import cane.brothers.gpt.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component(ReplyCallbackModelOpenaiCommand.NAME)
@ConditionalOnProperty(prefix = "openai", name = "api-key")
class ReplyCallbackModelOpenaiCommand extends AbstractReplyCallbackModelCommand {
    public final static String NAME = "/callback_model_openai";

    public ReplyCallbackModelOpenaiCommand(ChatSettings botSettings,
                                             TelegramClient telegramClient,
                                             ChatCallbackCommandFactory callbackFactory) {
        super(botSettings, telegramClient, callbackFactory);
    }
    @Override
    GptModel getModel() {
        return GptModel.OPENAI;
    }
}
