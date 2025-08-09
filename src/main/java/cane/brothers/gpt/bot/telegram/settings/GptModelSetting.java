package cane.brothers.gpt.bot.telegram.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing settings for selecting the GPT model used by the tgbot.
 * Each setting has a default value of type GptModel.
 * Example: CALLBACK_MODEL can be OPENAI or GEMINI.
 */
@Getter
@RequiredArgsConstructor
public enum GptModelSetting implements CommandSetting<GptModel> {
    MODEL(GptModel.GEMINI);

    private final GptModel defaultValue;

    public static GptModelSetting fromString(String command) {
        if (command.startsWith("/")) {
            var cmd = command.substring(1).toUpperCase();
            return GptModelSetting.valueOf(cmd);
        } else {
            throw new IllegalArgumentException("There is no relevant setting defined for the command \"%s\" "
                    .formatted(command));
        }
    }

    @Override
    public String getMessage(GptModel commandValue) {
        return String.format("%s gpt model is active", commandValue);
    }

    @Override
    public GptModel getDefaultValue() {
        return defaultValue;
    }
} 