package cane.brothers.gpt.bot.telegram.commands;

import cane.brothers.gpt.bot.telegram.settings.ChatSettingsCommand;
import cane.brothers.gpt.bot.telegram.settings.GptModel;
import cane.brothers.gpt.bot.telegram.settings.GptModelSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static cane.brothers.gpt.bot.telegram.commands.ReplyCallbackSettingsModelsCommand.NAME;

@Slf4j
@Component(NAME)
@RequiredArgsConstructor
public class ReplyCallbackSettingsModelsCommand implements ChatCommand<CallbackQuery> {

    public static final String NAME = "/callback_models_settings";

    private final TelegramClient telegramClient;

    @Override
    public void execute(CallbackQuery data) throws TelegramApiException {
        var chatId = data.getMessage().getChatId();
        log.debug("Chat: %d. Show callback models settings menu".formatted(chatId));

        // TODO commands chain
        ChatCommand<CallbackQuery> callbackAnswer = new ReplyCallbackAnswerCommand(telegramClient);
        callbackAnswer.execute(data);

        var reply = EditMessageText.builder().chatId(chatId)
                .messageId(data.getMessage().getMessageId())
                .parseMode(ParseMode.MARKDOWNV2)
                .text("*Models:*")
                .replyMarkup(getModelsSettingsKeyboard())
                .build();

        telegramClient.execute(reply);
    }

    InlineKeyboardMarkup getModelsSettingsKeyboard() {
        var openaiButton = InlineKeyboardButton.builder().text("Open AI")
                .callbackData("/callback_model_openai").build();

        var geminiButton = InlineKeyboardButton.builder().text("Gemini")
                .callbackData("/callback_model_gemini").build();

        var hideButton = InlineKeyboardButton.builder().text("Hide settings")
                .callbackData("/callback_hide_settings").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(List.of(openaiButton, geminiButton)))
                .keyboardRow(new InlineKeyboardRow(hideButton))
                .build();
    }
} 