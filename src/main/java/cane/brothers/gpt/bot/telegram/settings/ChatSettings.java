package cane.brothers.gpt.bot.telegram.settings;

/**
 * Interface for managing chat settings.
 * <p>
 * This interface combines the query and command parts of the CQRS (Command Query Responsibility Segregation) pattern.
 * Provides methods to get and set boolean and GPT model settings for a specific chat.
 */
public interface ChatSettings extends ChatSettingsQuery, ChatSettingsCommand {}
