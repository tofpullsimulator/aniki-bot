package org.eos.aniki.bot.services.rule34;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eos.aniki.bot.services.PostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the Gelbooru API service.
 *
 * @author Eos
 */
@Configuration
public class Rule34Config {

    /**
     * The Rule34 post repository.
     *
     * @return A repository configured to talk with the Rule34 API service.
     */
    @Bean("rule34")
    public PostRepository rule34() {
        String apiUrl = "api.rule34.xxx";
        String baseUrl = "rule34.xxx";
        WebClient client = client();

        return new Rule34Repository("https", apiUrl, baseUrl, 443, client);
    }

    /**
     * Configure a client to talk to the Rule34 API service with.
     *
     * @return A configured web client.
     */
    public WebClient client() {
        int size = 16 * 1024 * 1024;
        var strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.customCodecs()
                        .register(new Jackson2JsonDecoder(new ObjectMapper(), MediaType.TEXT_HTML)))
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
