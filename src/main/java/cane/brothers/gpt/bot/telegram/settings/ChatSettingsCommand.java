package cane.brothers.gpt.bot.telegram.settings;

/**
 * Command interface for updating chat settings.
 * <p>
 * This interface represents the command part of the CQRS (Command Query Responsibility Segregation) pattern.
 * Provides methods to update boolean commands and set settings of any type for a specific chat.
 */
public interface ChatSettingsCommand {
    // apply command only to boolean commands
    Boolean updateCommand(Long chatId, String command);

    <T> void setChatSetting(Long chatId, CommandSetting<T> setting, T value);
} 