package org.eos.aniki.bot.services.gelbooru;

import org.eos.aniki.bot.services.PostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the Gelbooru API service.
 *
 * @author Eos
 */
@Configuration
public class GelbooruConfig {

    /**
     * The Gelbooru post repository.
     *
     * @return A repository configured to talk with the Gelbooru API service.
     */
    @Bean("gelbooru")
    public PostRepository gelbooru() {
        String baseUrl = "https://gelbooru.com";
        WebClient client = client(baseUrl);

        return new GelbooruRepository(client);
    }

    /**
     * Configure a client to talk to the Gelbooru API service with.
     *
     * @param endpoint The API endpoint to talk with.
     * @return A configured web client.
     */
    public WebClient client(final String endpoint) {
        int size = 16 * 1024 * 1024;
        var strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .baseUrl(endpoint)
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
