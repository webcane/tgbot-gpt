package cane.brothers.gpt.bot.telegram.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

interface KeyboardMarkup {

    default InlineKeyboardMarkup getSettingsKeyboard() {
        var complexityButton = InlineKeyboardButton.builder().text("Reply")
                .callbackData("/callback_reply").build();

        var resultsButton = InlineKeyboardButton.builder().text("Markup")
                .callbackData("/callback_markup").build();

        var modelsButton = InlineKeyboardButton.builder().text("Models")
                .callbackData("/callback_models_settings").build();

        var hideButton = InlineKeyboardButton.builder().text("Hide settings")
                .callbackData("/callback_hide_settings").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(List.of(complexityButton, resultsButton, modelsButton)))
                .keyboardRow(new InlineKeyboardRow(hideButton))
                .build();
    }
}
