package cane.brothers.gpt.bot.telegram.commands.model;

import cane.brothers.gpt.bot.telegram.commands.ChatCallbackCommandFactory;
import cane.brothers.gpt.bot.telegram.settings.ChatSettings;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component(ReplyCallbackModelGeminiCommand.NAME)
@ConditionalOnProperty(prefix = "google.cloud", name = "project-id")
class ReplyCallbackModelGeminiCommand extends AbstractReplyCallbackModelCommand {

    public final static String NAME = "/callback_model_gemini" ;

    public ReplyCallbackModelGeminiCommand(ChatSettings botSettings,
                                           TelegramClient telegramClient,
                                           ChatCallbackCommandFactory callbackFactory) {
        super(botSettings, telegramClient, callbackFactory);
    }

    @Override
    GptModel getModel() {
        return GptModel.GEMINI;
    }
} 