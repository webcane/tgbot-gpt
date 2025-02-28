package cane.brothers.gpt.bot.openai;

import org.springframework.ai.autoconfigure.openai.OpenAiAudioTranscriptionProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Configuration
class OpenAiConfig {

    @Bean
    SimpleLoggerAdvisor loggerAdvisor() {
        //final Function<AdvisedRequest, String> request_to_string = request -> request.userText();
        //final Function<ChatResponse, String> response_to_string = response -> response.getResult().getOutput().getContent();
        //return new SimpleLoggerAdvisor(request_to_string, response_to_string, 0);
        return new SimpleLoggerAdvisor();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, SimpleLoggerAdvisor loggerAdvisor) {
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
                //.webClientBuilder((WebClient.Builder)webClientBuilderProvider.getIfAvailable(WebClient::builder))
                .responseErrorHandler(responseErrorHandler).build();
    }

    @Bean
    OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel(OpenAiAudioApi openAiAudioApi, OpenAiAudioTranscriptionProperties transcriptionProperties, RetryTemplate retryTemplate) {
        return new OpenAiAudioTranscriptionModel(openAiAudioApi, transcriptionProperties.getOptions(), retryTemplate);
    }
}
