package cane.brothers.ai.bot.telegram.commands;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

interface KeyboardMarkup {

    default InlineKeyboardMarkup getSettingsKeyboard() {
        // ChatCommandCallbackFactory.MENU_REPLY.toString()
        var complexityButton = InlineKeyboardButton.builder().text("Reply")
                .callbackData("/callback_reply").build();

        // ChatCommandCallbackFactory.MENU_MARKUP.toString()
        var resultsButton = InlineKeyboardButton.builder().text("Markup")
                .callbackData("/callback_markup").build();

        // ChatCommandCallbackFactory.MENU_HIDE_SETTINGS.toString()
        var hideButton = InlineKeyboardButton.builder().text("Hide settings")
                .callbackData("/callback_hide_settings").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(List.of(complexityButton, resultsButton)))
                .keyboardRow(new InlineKeyboardRow(hideButton))
                .build();
    }
}
