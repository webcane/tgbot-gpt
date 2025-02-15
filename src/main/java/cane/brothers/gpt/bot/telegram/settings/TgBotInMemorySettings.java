package cane.brothers.gpt.bot.telegram.settings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
class TgBotInMemorySettings implements ChatSettings {

    private static final Map<Long, Map<CommandSetting, Boolean>> settings = new HashMap<>();

    @Override
    public Boolean getUseMarkup(Long chatId) {
        return getChatSettings(chatId, BooleanSetting.MARKUP);
    }

    @Override
    public Boolean getUseReply(Long chatId) {
        return getChatSettings(chatId, BooleanSetting.REPLY);
    }

    @Override
    public Boolean updateCommand(Long chatId, String command) {
        var setting = BooleanSetting.fromString(command);
        return settings.computeIfAbsent(chatId, chat -> new HashMap<>())
                .merge(setting, !setting.getDefaultValue(), (oldValue, newValue) -> !oldValue);
    }

    Boolean getChatSettings(Long chatId, CommandSetting command) {
        var bool = settings.computeIfAbsent(chatId, chat -> new HashMap<>())
                .computeIfAbsent(command, CommandSetting::getDefaultValue);
        log.debug(command.getMessage(bool));
        return bool;
    }
}
