package cane.brothers.gpt.bot.telegram.settings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
class TgBotInMemorySettings implements ChatSettings {

    private static final Map<Long, Map<CommandSetting<?>, Object>> settings = new HashMap<>();

    // query methods
    @Override
    public Boolean getUseMarkup(Long chatId) {
        return getChatSettings(chatId, BooleanSetting.MARKUP);
    }

    @Override
    public Boolean getUseReply(Long chatId) {
        return getChatSettings(chatId, BooleanSetting.REPLY);
    }

    @Override
    public GptModel getGptModel(Long chatId) {
        return getChatSettings(chatId, GptModelSetting.MODEL);
    }

    @SuppressWarnings("unchecked")
    <T> T getChatSettings(Long chatId, CommandSetting<T> command) {
        var value = settings.computeIfAbsent(chatId, chat -> new HashMap<>())
                .computeIfAbsent(command, c -> command.getDefaultValue());
        log.debug(command.getMessage((T) value));
        return (T) value;
    }

    // command methods
    @Override
    public Boolean updateCommand(Long chatId, String command) {
       var setting = BooleanSetting.fromString(command);
        var chatSettings = settings.get(chatId);
        var oldValue = (Boolean) chatSettings.get(setting);
        var value = oldValue != null ? !oldValue : setting.getDefaultValue();
        setChatSetting(chatId, setting, value);
        return value;
    }

    @Override
    public <T> void setChatSetting(Long chatId, CommandSetting<T> setting, T value) {
        settings
            .computeIfAbsent(chatId, k -> new HashMap<>())
            .put(setting, value);
    }
}
