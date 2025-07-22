package cane.brothers.gpt.bot.telegram.settings;

/**
 * Interface for querying chat settings.
 * <p>
 * This interface represents the query part of the CQRS (Command Query Responsibility Segregation) pattern.
 * Provides methods to get boolean and GPT model settings for a specific chat.
 */
public interface ChatSettingsQuery {
    Boolean getUseMarkup(Long chatId);
    Boolean getUseReply(Long chatId);
    GptModel getGptModel(Long chatId);
} 