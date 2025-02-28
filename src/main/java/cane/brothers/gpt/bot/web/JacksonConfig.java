package cane.brothers.gpt.bot.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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
                .registerModule(new JavaTimeModule());
        objMapper.configOverride(ByteArrayOutputStream.class)
                .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY));
        return objMapper;
    }
}
