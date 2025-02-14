package cane.brothers.ai.bot.telegram.settings;

public interface ChatSettings {

    Boolean getUseMarkup(Long chatId);

    Boolean getUseReply(Long chatId);

    Boolean updateCommand(Long chatId, String command);

}
