package cane.brothers.gpt.bot.ai.deepseek;

import io.micrometer.observation.ObservationRegistry;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@EnableConfigurationProperties({DeepSeekConnectionProperties.class, DeepSeekChatProperties.class})
public class DeepSeekConfig {

    @Bean
    DeepSeekApi deepSeekApi(DeepSeekConnectionProperties properties) {
        return DeepSeekApi.builder()
                .apiKey(properties.getApiKey())
                .build();
    }

    @Bean
    DeepSeekChatModel deepSeekChatModel(DeepSeekChatProperties chatProperties, DeepSeekConnectionProperties connProperties) {
        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi(connProperties))
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

    @Bean("deepSeekChatClient")
    ChatClient chatClient(@Qualifier("deepSeekChatClientBuilder") ChatClient.Builder builder,
                          SimpleLoggerAdvisor loggerAdvisor) {
        return builder
                .defaultAdvisors(loggerAdvisor)
                .build();
    }
}
