package cane.brothers.gpt.bot.ai.openai;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.openai.autoconfigure.OpenAIAutoConfigurationUtil;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Slf4j
@Configuration
@EnableConfigurationProperties({OpenAiConnectionProperties.class, OpenAiAudioTranscriptionProperties.class})
class OpenAiVoiceConfig {

    @Bean
    OpenAiAudioApi openAiAudioApi(OpenAiConnectionProperties commonProperties,
                                  RestClient.Builder restClientBuilder,
                                  ResponseErrorHandler responseErrorHandler) {
        return OpenAiAudioApi.builder()
                .apiKey(commonProperties.getApiKey())
                .baseUrl(commonProperties.getBaseUrl())
                .restClientBuilder(restClientBuilder)
                .responseErrorHandler(responseErrorHandler).build();
    }

    @Bean("openaiVoiceClient")
    OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel(OpenAiAudioApi openAiAudioApi,
                                                                OpenAiAudioTranscriptionProperties transcriptionProperties,
                                                                RetryTemplate retryTemplate) {
        log.debug("create Open AI Voice ChatClient");
        return new OpenAiAudioTranscriptionModel(openAiAudioApi,
                transcriptionProperties.getOptions(),
                retryTemplate);
    }
}
