package cane.brothers.gpt.bot.ai.gemini;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiChatProperties;
import org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiConnectionProperties;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "google.cloud", name = "project-id")
@EnableConfigurationProperties({VertexAiGeminiConnectionProperties.class, VertexAiGeminiChatProperties.class})
class GeminiConfig {

    @Bean
    public VertexAI vertexAi(VertexAiGeminiConnectionProperties connectionProperties) throws IOException {
        Assert.hasText(connectionProperties.getProjectId(), "Vertex AI project-id must be set!");
        Assert.hasText(connectionProperties.getLocation(), "Vertex AI location must be set!");
        Assert.notNull(connectionProperties.getTransport(), "Vertex AI transport must be set!");
        VertexAI.Builder vertexAIBuilder = (new VertexAI.Builder())
                .setProjectId(connectionProperties.getProjectId())
                .setLocation(connectionProperties.getLocation())
                .setTransport(Transport.valueOf(connectionProperties.getTransport().name()));
        if (StringUtils.hasText(connectionProperties.getApiEndpoint())) {
            vertexAIBuilder.setApiEndpoint(connectionProperties.getApiEndpoint());
        }

        if (!CollectionUtils.isEmpty(connectionProperties.getScopes())) {
            vertexAIBuilder.setScopes(connectionProperties.getScopes());
        }

        if (connectionProperties.getCredentialsUri() != null) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(connectionProperties
                    .getCredentialsUri()
                    .getInputStream());
            vertexAIBuilder.setCredentials(credentials);
        }

        return vertexAIBuilder.build();
    }

    @Bean
    public VertexAiGeminiChatModel vertexAiGeminiChat(VertexAI vertexAi,
                                                      VertexAiGeminiChatProperties chatProperties,
                                                      ToolCallingManager toolCallingManager,
                                                      RetryTemplate retryTemplate,
                                                      ObjectProvider<ObservationRegistry> observationRegistry,
                                                      ObjectProvider<ChatModelObservationConvention> observationConvention,
                                                      ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicate) {
        VertexAiGeminiChatModel chatModel = VertexAiGeminiChatModel.builder()
                .vertexAI(vertexAi)
                .defaultOptions(chatProperties.getOptions())
                .toolCallingManager(toolCallingManager)
                .toolExecutionEligibilityPredicate(
                        toolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
                .retryTemplate(retryTemplate)
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    @Scope("prototype")
    @Bean("geminiChatClientBuilder")
    ChatClient.Builder chatClientBuilder(VertexAiGeminiChatModel geminiChatModel,
                                         ObjectProvider<ObservationRegistry> observationRegistry,
                                         ObjectProvider<ChatClientObservationConvention> observationConvention) {

        return ChatClient.builder(geminiChatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConvention.getIfUnique(() -> null));
    }

    @Bean("geminiChatClient")
    ChatClient chatClient(@Qualifier("geminiChatClientBuilder") ChatClient.Builder builder,
                          SimpleLoggerAdvisor loggerAdvisor) {
        log.debug("create Gemini ChatClient");
        return builder
                .defaultAdvisors(loggerAdvisor)
                .build();
    }
}
