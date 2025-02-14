package cane.brothers.ai.bot.telegram.settings;

interface CommandSetting {
    default String getMessage(Boolean useCommand) {
        return String.format("%s is %s", this, useCommand ? "active" : "inactive");
    }

    Boolean getDefaultValue();
}
