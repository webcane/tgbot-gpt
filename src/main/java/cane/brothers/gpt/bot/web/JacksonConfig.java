package cane.brothers.gpt.bot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
class JacksonConfig {

    @Bean
    ObjectMapper objectMapper() {
        var objMapper = JsonMapper.builder().build()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .registerModule(new JavaTimeModule());
        return objMapper;
    }

    @Bean
    public HttpMessageConverters customConverters(MappingJackson2HttpMessageConverter httpMessageConverter) {
        return new HttpMessageConverters(httpMessageConverter);
    }

    @Bean
    MappingJackson2HttpMessageConverter httpMessageConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
