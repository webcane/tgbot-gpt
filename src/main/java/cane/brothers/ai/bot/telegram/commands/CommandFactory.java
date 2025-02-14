package cane.brothers.ai.bot.telegram.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;

import javax.validation.constraints.NotNull;

interface CommandFactory<T extends BotApiObject> {

    ChatCommand<T> create(String message);

    default ChatCommand<T> create(@NotNull Class<? extends ChatCommand<T>> commandClass) {
        if (commandClass.isAnnotationPresent(Component.class)) {
            Component componentAnnotation = commandClass.getClass().getAnnotation(Component.class);
            create(componentAnnotation.value());
        }
        throw new IllegalArgumentException("The command name is not defined for class %s"
                .formatted(commandClass.getName()));
    }
}
