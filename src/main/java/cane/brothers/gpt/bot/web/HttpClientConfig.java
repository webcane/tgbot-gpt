package cane.brothers.gpt.bot.web;

import cane.brothers.gpt.bot.AppProperties;
import jakarta.annotation.PreDestroy;
import org.eclipse.jetty.client.*;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.http.client.JettyClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.JettyHttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Configuration
class HttpClientConfig {

    private HttpClient jettyHttpClient;

    @Bean
    public JettyHttpClientBuilder builder() {
        return new JettyHttpClientBuilder()
                .withHttpClientTransportCustomizer(t -> new HttpClientTransportOverHTTP(1));
    }

//    @Bean
//    public ConnectionPool connectionPool() {
//        return new ConnectionPool(100, 75, TimeUnit.SECONDS);
//    }

    @Bean
    public HttpClientSettings settings() {
//        jettyHttpClient.setMaxConnectionsPerDestination(5);
//        jettyHttpClient.setMaxRequestsQueuedPerDestination(100);
//        jettyHttpClient.setConnectTimeout(5000);

        // create fine-tuned Jetty HttpClient
//        QueuedThreadPool threadPool = new QueuedThreadPool(4);
//        threadPool.setName("jetty-client-qtp");
//        threadPool.setVirtualThreadsExecutor(VirtualThreads.getNamedVirtualThreadsExecutor("jcvt-"));
//        httpClient.setExecutor(threadPool);

//        Dispatcher dispatcher = new Dispatcher();
//        dispatcher.setMaxRequests(100);
//        dispatcher.setMaxRequestsPerHost(100);
        return new HttpClientSettings(
                HttpRedirects.FOLLOW_WHEN_POSSIBLE,
                Duration.ofSeconds(75),
                Duration.ofSeconds(100),
                null);
    }

    @Bean
    public HttpClient jettyHttpClient(JettyHttpClientBuilder jettyBuilder,
                                      HttpClientSettings settings,
                                      Supplier<ProxyConfiguration.Proxy> proxySupplier,
                                      Supplier<Authentication> authenticatorSupplier) throws Exception {
        jettyBuilder.withCustomizer(c ->
                Optional.ofNullable(proxySupplier.get())
                        .ifPresent(jettyHttpClient.getProxyConfiguration()::addProxy));
        jettyBuilder.withCustomizer(c ->
                Optional.ofNullable(authenticatorSupplier.get())
                        .ifPresent(jettyHttpClient.getAuthenticationStore()::addAuthentication));
        var jettyHttpClient = jettyBuilder.build(settings);

        jettyHttpClient.start();
        return jettyHttpClient;
    }

    @Bean
    Supplier<ProxyConfiguration.Proxy> proxySupplier(AppProperties properties) {
        // Proxy configuration
        return properties.proxy() == null ?
                () -> null :
                () -> {
                    if ("http".equals(properties.proxy().method())) {
                        return new HttpProxy(properties.proxy().hostname(), properties.proxy().port());
                    } else if ("socks5".equals(properties.proxy().method())) {
                        var proxy = new Socks5Proxy(properties.proxy().hostname(), properties.proxy().port());
                        var socks5AuthenticationFactory = new Socks5.UsernamePasswordAuthenticationFactory(
                                properties.proxy().username(),
                                properties.proxy().password());
                        // Add the authentication method to the proxy.
                        proxy.putAuthenticationFactory(socks5AuthenticationFactory);
                        return proxy;
                    } else {
                        return null;
                    }
                };
    }

    @Bean
    Supplier<Authentication> authenticatorSupplier(AppProperties properties) {
        // Proxy credentials
        return properties.proxy() == null ?
                () -> null :
                () -> {
                    if ("http".equals(properties.proxy().method())) {
                        try {
                            URI proxyURI = new URI("http://%s:%d".formatted(
                                    properties.proxy().hostname(),
                                    properties.proxy().port()));
                            return new BasicAuthentication(proxyURI, "ProxyRealm",
                                    properties.proxy().username(),
                                    properties.proxy().password());
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        return null;
                    }
                };
    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (jettyHttpClient != null) {
            // gracefully stop when the application shuts down
            jettyHttpClient.stop();
        }
    }

    @Bean
    RestClient.Builder restClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer,
                                         JettyClientHttpRequestFactoryBuilder requestFactoryBuilder) {
        RestClient.Builder builder = RestClient.builder()
                .requestFactory(requestFactoryBuilder.build());
        return restClientBuilderConfigurer.configure(builder);
    }
}
