package cane.brothers.gpt.bot.telegram.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing settings for boolean values.
 * Each setting has a default value of type Boolean.
 * Example: CALLBACK_REPLY can be true or false.
 */
@Getter
@RequiredArgsConstructor
enum BooleanSetting implements CommandSetting<Boolean> {
    CALLBACK_REPLY(Boolean.TRUE),
    CALLBACK_MARKUP(Boolean.TRUE);

    private final Boolean defaultValue;

    @Override
    public String getMessage(Boolean useCommand) {
        return String.format("%s is %s", this, useCommand ? "active" : "inactive");
    }

    public static BooleanSetting fromString(String command) {
        if (command.startsWith("/")) {
            var cmd = command.substring(1).toUpperCase();
            return BooleanSetting.valueOf(cmd);
        } else {
            throw new IllegalArgumentException("There is no relevant setting defined for the command \"%s\" "
                    .formatted(command));
        }
    }
}
