package cane.brothers.ai.bot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JacksonConfig {

    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().build()
                .registerModule(new JavaTimeModule());
    }
}
