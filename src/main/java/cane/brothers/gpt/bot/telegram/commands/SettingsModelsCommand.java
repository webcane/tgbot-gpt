package cane.brothers.gpt.bot.telegram.commands;

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

import static cane.brothers.gpt.bot.telegram.commands.SettingsModelsCommand.NAME;


@Slf4j
@Component(NAME)
@RequiredArgsConstructor
class SettingsModelsCommand implements ChatCommand<Message> {

    public static final String NAME = "/models";
    private final TelegramClient telegramClient;

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
        var openaiButton = InlineKeyboardButton.builder().text("Open AI")
                .callbackData("/callback_model_openai").build();

        var geminiButton = InlineKeyboardButton.builder().text("Gemini")
                .callbackData("/callback_model_gemini").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(openaiButton))
                .keyboardRow(new InlineKeyboardRow(geminiButton))
                .build();
    }
}
