package cane.brothers.gpt.bot.web;

import cane.brothers.gpt.bot.AppProperties;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Configuration
class HttpClientConfig {

    @Bean
    Supplier<Proxy> proxySupplier(AppProperties properties) {
        return properties.proxy() == null ?
                () -> null :
                () -> new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(properties.proxy().hostname(),
                                properties.proxy().port() == null ? 0 : properties.proxy().port()));
    }

    @Bean
    Supplier<okhttp3.Authenticator> authenticatorSupplier(AppProperties properties) {
        return properties.proxy() == null ?
                () -> null :
                () -> (route, response) -> {
                    String credential = Credentials.basic(properties.proxy().username(), properties.proxy().password());
                    return response
                            .request()
                            .newBuilder()
                            .header(HttpHeaders.PROXY_AUTHORIZATION, credential)
                            .build();
                };
    }

    @Bean
    public ConnectionPool connectionPool() {
        return new ConnectionPool(100, 75, TimeUnit.SECONDS);
    }

    @Bean
    public OkHttpClient.Builder okHttpClientBuilder(Supplier<Proxy> proxySupplier,
                                                    Supplier<okhttp3.Authenticator> authenticatorSupplier,
                                                    ConnectionPool connectionPool) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(100);
        dispatcher.setMaxRequestsPerHost(100);

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient()
                .newBuilder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(70, TimeUnit.SECONDS)
                .connectTimeout(75, TimeUnit.SECONDS);

        // Proxy
        Optional.ofNullable(proxySupplier.get()).ifPresent(okHttpClientBuilder::proxy);
        Optional.ofNullable(authenticatorSupplier.get()).ifPresent(okHttpClientBuilder::proxyAuthenticator);

        return okHttpClientBuilder;
    }

    @Bean
    public OkHttpClient okHttpClient(OkHttpClient.Builder okHttpClientBuilder) {
        return okHttpClientBuilder.build();
    }

    @Bean
    OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(OkHttpClient okHttpClient) {
        return new OkHttp3ClientHttpRequestFactory(okHttpClient);
    }

    @Bean
    RestClient.Builder restClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer,
                                         OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory) {
        RestClient.Builder builder = RestClient.builder()
                .requestFactory(okHttp3ClientHttpRequestFactory);
        return restClientBuilderConfigurer.configure(builder);
    }
}
