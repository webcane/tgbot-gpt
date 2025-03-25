package cane.brothers.gpt.bot.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.ByteArrayOutputStream;

@Configuration
class JacksonConfig {

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        var objMapper = JsonMapper.builder().build()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // to avoid UnrecognizedPropertyException: Unrecognized field "annotations"
                // (class org.springframework.ai.openai.api.OpenAiApi$ChatCompletionMessage)
                // see https://github.com/spring-projects/spring-ai/pull/2460
                // see https://github.com/spring-projects/spring-ai/issues/2449
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
        objMapper.configOverride(ByteArrayOutputStream.class)
                .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY));
        return objMapper;
    }
}
