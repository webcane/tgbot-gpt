package cane.brothers.gpt.bot.telegram.commands.model;

import cane.brothers.gpt.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component(ReplyCallbackModelDeepseekCommand.NAME)
@ConditionalOnProperty(prefix = "deepseek", name = "api-key")
class ReplyCallbackModelDeepseekCommand extends AbstractReplyCallbackModelCommand {

    public final static String NAME = "/callback_model_deepseek" ;

    public ReplyCallbackModelDeepseekCommand(ChatSettings botSettings,
                                             TelegramClient telegramClient,
                                             ChatCallbackCommandFactory callbackFactory) {
        super(botSettings, telegramClient, callbackFactory);
    }

    @Override
    GptModel getModel() {
        return GptModel.DEEPSEEK;
    }
}
