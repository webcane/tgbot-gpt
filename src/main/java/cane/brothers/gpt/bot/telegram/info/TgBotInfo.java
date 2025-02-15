package cane.brothers.gpt.bot.telegram.info;

public record TgBotInfo(boolean ok, TgBot result) {

    public String getBotUsername() {
        return "@" + result.username;
    }

    record TgBot(long id,
                 boolean isBot,
                 String firstName,
                 String username,
                 boolean canJoinGroups,
                 boolean canReadAllGroupMessages,
                 boolean supportsInlineQueries,
                 boolean canConnectToBusiness,
                 boolean hasMainWebApp) {
    }
}

