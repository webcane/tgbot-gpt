package cane.brothers.gpt.bot.telegram.settings;

/**
 * Interface for command settings.
 * <p>
 * This interface provides methods to get the default value of a setting and to format a message about the setting.
 */
interface CommandSetting<T> {

    String getMessage(T commandValue);

    T getDefaultValue();
}
