package cane.brothers.gpt.bot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
class JacksonConfig implements WebMvcConfigurer {

    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().build()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .registerModule(new JavaTimeModule());
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        converters.add(converter);
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
