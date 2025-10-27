package cane.brothers.gpt.bot.ai.deepseek;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatProperties;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekConnectionProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "deepseek", name = "api-key")
@EnableConfigurationProperties({DeepSeekConnectionProperties.class, DeepSeekChatProperties.class})
public class DeepSeekConfig {

    @Bean
    DeepSeekApi deepSeekApi(DeepSeekConnectionProperties properties,
                            RestClient.Builder restClientBuilder,
                            ResponseErrorHandler responseErrorHandler) {
        return DeepSeekApi.builder()
                .apiKey(properties.getApiKey())
                .restClientBuilder(restClientBuilder)
                .responseErrorHandler(responseErrorHandler)
                .build();
    }

    @Bean
    DeepSeekChatModel deepSeekChatModel(DeepSeekChatProperties chatProperties, DeepSeekApi deepSeekApi) {
        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .defaultOptions(chatProperties.getOptions())
                .build();
    }

    @Scope("prototype")
    @Bean("deepSeekChatClientBuilder")
    ChatClient.Builder chatClientBuilder(DeepSeekChatModel deepSeekChatModel,
                                         ObjectProvider<ObservationRegistry> observationRegistry,
                                         ObjectProvider<ChatClientObservationConvention> observationConvention) {

        return ChatClient.builder(deepSeekChatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConvention.getIfUnique(() -> null));
    }

    @Bean("deepseekChatClient")
    ChatClient chatClient(@Qualifier("deepSeekChatClientBuilder") ChatClient.Builder builder,
                          SimpleLoggerAdvisor loggerAdvisor) {
        log.debug("create DeepSeek ChatClient");
        return builder
                .defaultAdvisors(loggerAdvisor)
                .build();
    }
}
