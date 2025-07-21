package cane.brothers.gpt.bot.ai;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoggingConfig {

    @Bean
    SimpleLoggerAdvisor loggerAdvisor() {
        //final Function<AdvisedRequest, String> request_to_string = request -> request.userText();
        //final Function<ChatResponse, String> response_to_string = response -> response.getResult().getOutput().getContent();
        //return new SimpleLoggerAdvisor(request_to_string, response_to_string, 0);
        return new SimpleLoggerAdvisor();
    }
}
