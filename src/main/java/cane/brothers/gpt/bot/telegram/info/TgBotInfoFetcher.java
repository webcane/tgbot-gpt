package cane.brothers.gpt.bot.telegram.info;

import cane.brothers.gpt.bot.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TgBotInfoFetcher {

    private static final String BASE_URL = "https://api.telegram.org/bot";
    private final AppProperties properties;
    private final OkHttpClient okClient;
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

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = okClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException ex) {
            log.error("Unable to fetch bot info", ex);
        }
        return null;
    }
}
