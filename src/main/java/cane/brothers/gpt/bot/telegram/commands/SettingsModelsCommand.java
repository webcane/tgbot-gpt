package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.ai.ChatClientService;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Slf4j
@Component(SettingsModelsCommand.NAME)
@RequiredArgsConstructor
class SettingsModelsCommand implements ChatCommand<Message> {

    public static final String NAME = "/models" ;
    private final TelegramClient telegramClient;
    private final ChatClientService chatClient;

    @Override
    public void execute(Message data) throws TelegramApiException {
        Long chatId = data.getChatId();
        log.debug("Chat: {}. Show model settings", chatId);

        var reply = SendMessage.builder().chatId(chatId)
                .parseMode(ParseMode.HTML)
                .text("Use one of the <b>AI models</b> bellow to ask your questions")
                .replyMarkup(getModelsKeyboard())
                .build();

        telegramClient.execute(reply);
    }

    InlineKeyboardMarkup getModelsKeyboard() {
        var keyBoardBuilder = InlineKeyboardMarkup.builder();

        if (chatClient.hasClientForModel(GptModel.OPENAI)) {
            var openaiButton = InlineKeyboardButton.builder().text("Open AI")
                    .callbackData("/callback_model_openai").build();
            keyBoardBuilder.keyboardRow(new InlineKeyboardRow(openaiButton));
        }

        if (chatClient.hasClientForModel(GptModel.GEMINI)) {
            var geminiButton = InlineKeyboardButton.builder().text("Gemini")
                    .callbackData("/callback_model_gemini").build();
            keyBoardBuilder.keyboardRow(new InlineKeyboardRow(geminiButton));
        }

        if (chatClient.hasClientForModel(GptModel.DEEPSEEK)) {
            var deepseekButton = InlineKeyboardButton.builder().text("DeepSeek")
                    .callbackData("/callback_model_deepseek").build();
            keyBoardBuilder.keyboardRow(new InlineKeyboardRow(deepseekButton));
        }

        return keyBoardBuilder.build();
    }
}
