package cane.brothers.gpt.bot.ai.openai;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties({OpenAiConnectionProperties.class, OpenAiChatProperties.class})
class OpenAiConfig {

    @Bean
    OpenAiApi openAiApi(OpenAiChatProperties chatProperties, OpenAiConnectionProperties commonProperties,
                        RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler,
                        String modelType) {
        OpenAIAutoConfigurationUtil.ResolvedConnectionProperties resolved = OpenAIAutoConfigurationUtil
                .resolveConnectionProperties(commonProperties, chatProperties, modelType);
        return OpenAiApi.builder()
                .baseUrl(resolved.baseUrl())
                .apiKey(new SimpleApiKey(resolved.apiKey()))
                .headers(resolved.headers())
                .completionsPath(chatProperties.getCompletionsPath())
                .embeddingsPath("/v1/embeddings")
                .restClientBuilder(restClientBuilder)
                .responseErrorHandler(responseErrorHandler)
                .build();
    }

    @Bean
    OpenAiChatModel openAiChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties,
                                    ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                    ToolCallingManager toolCallingManager,
                                    RetryTemplate retryTemplate,
                                    ResponseErrorHandler responseErrorHandler,
                                    ObjectProvider<ObservationRegistry> observationRegistry,
                                    ObjectProvider<ChatModelObservationConvention> observationConvention,
                                    ObjectProvider<ToolExecutionEligibilityPredicate> openAiToolExecutionEligibilityPredicate) {
        OpenAiApi openAiApi = this.openAiApi(chatProperties,
                commonProperties,
                restClientBuilderProvider.getIfAvailable(RestClient::builder),
                responseErrorHandler, "chat");

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(chatProperties.getOptions())
                .toolCallingManager(toolCallingManager)
                .toolExecutionEligibilityPredicate((ToolExecutionEligibilityPredicate) openAiToolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
                .retryTemplate(retryTemplate).observationRegistry((ObservationRegistry) observationRegistry.getIfUnique(() -> {
                    return ObservationRegistry.NOOP;
                })).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    @Scope("prototype")
    @Bean("openAiChatClientBuilder")
    ChatClient.Builder chatClientBuilder(OpenAiChatModel openAiChatModel,
                                         ObjectProvider<ObservationRegistry> observationRegistry,
                                         ObjectProvider<ChatClientObservationConvention> observationConvention) {

        return ChatClient.builder(openAiChatModel, observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConvention.getIfUnique(() -> null));
    }

    @Bean("openAiChatClient")
    ChatClient chatClient(@Qualifier("openAiChatClientBuilder") ChatClient.Builder builder,
                          SimpleLoggerAdvisor loggerAdvisor) {
        return builder
                .defaultAdvisors(loggerAdvisor)
                .build();
    }

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

    @Bean("openAiVoiceChatClient")
    OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel(OpenAiAudioApi openAiAudioApi,
                                                                OpenAiAudioTranscriptionProperties transcriptionProperties,
                                                                RetryTemplate retryTemplate) {
        return new OpenAiAudioTranscriptionModel(openAiAudioApi,
                transcriptionProperties.getOptions(),
                retryTemplate);
    }
}
