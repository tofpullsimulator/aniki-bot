package org.eos.aniki.bot.services.r34;

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
public class R34Config {

    /**
     * The r34.xyz post repository.
     *
     * @return A repository configured to talk with the r34.xyz API service.
     */
    @Bean("r34xyz")
    public PostRepository r34xyz() {
        String baseUrl = "https://r-34.xyz";
        String cdnUrl = "https://r34xyz.b-cdn.net";
        WebClient client = client(baseUrl);

        return new R34Repository(baseUrl, cdnUrl, "thumbnailex", client);
    }

    /**
     * The anime.r34 post repository.
     *
     * @return A repository configured to talk with the anime.r34 API service.
     */
    @Bean("animer34")
    public PostRepository animer34() {
        String baseUrl = "https://anime.rule34.world";
        String cdnUrl = "https://anime2.b-cdn.net";
        WebClient client = client(baseUrl);

        return new R34Repository(baseUrl, cdnUrl, "thumbnail", client);
    }

    /**
     * Configure a client to talk to the r34.xyz/anime.r34 API service with.
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
