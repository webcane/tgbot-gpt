package cane.brothers.ai.bot.telegram.commands;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface ChatCommand<T extends BotApiObject> {

    // TODO list all commands

    void execute(T data) throws TelegramApiException;
}
