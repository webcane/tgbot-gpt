package cane.brothers.gpt.bot.telegram.info;

import cane.brothers.gpt.bot.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TgBotInfoFetcher {

    private static final String BASE_URL = "https://api.telegram.org/bot";
    private final AppProperties properties;
    private final HttpClient jettyHttpClient;
    private final ConversionService conversionSvc;

    public TgBotInfo getInfo() {
        String info = requestInfo();
        if (info != null) {
            return conversionSvc.convert(info, TgBotInfo.class);
        }
        return null;
    }

    String requestInfo() {
        String url = BASE_URL + properties.token() + "/getMe";
        try {
            ContentResponse response = jettyHttpClient.GET(url);
            if (response.getStatus() == HttpStatus.OK_200
                    && response.getContent() != null) {
                return response.getContentAsString();
            }
        } catch (Exception ex) {
            log.error("Unable to fetch bot info", ex);
        }
        return null;
    }
}
