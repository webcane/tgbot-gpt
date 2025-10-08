package cane.brothers.gpt.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "tgbot")
public record AppProperties(
        @NotNull(message = "The property 'tgbot.token' is required")
        String token, HttpProxy proxy,
        String voicePath,
        String infoUrl,
        List<String> allowedUserNames) {

    public record HttpProxy(
            @NotNull(message = "The property 'tgbot.proxy.hostname' is required")
            String hostname,
            @NotNull(message = "The property 'tgbot.proxy.port' is required")
            Integer port,
            String username,
            String password,
            String method) {
    }
}
