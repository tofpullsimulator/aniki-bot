package org.eos.aniki.bot.services.rule34;

import lombok.AllArgsConstructor;
import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.PostRepository;
import org.eos.aniki.bot.services.Tag;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Repository to fetch posts and tags via the Rule34 API service with.
 *
 * @author Eos
 */
@AllArgsConstructor
public class Rule34Repository implements PostRepository {

    private final String scheme;
    private final String apiUrl;
    private final String baseUrl;
    private final int port;
    private final WebClient client;

    /**
     * {@inheritDoc}
     */
    public Flux<Tag> getTags(final String keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Flux.empty();
        }

        return client.get()
                .uri(builder -> builder
                        .scheme(scheme)
                        .host(baseUrl)
                        .port(port)
                        .path("public/autocomplete.php")
                        .queryParam("q", keywords)
                        .build())
                .retrieve()
                .bodyToFlux(Rule34Response.Rule34Tag.class)
                .flatMap(Rule34Response.Rule34Tag::toTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Post> getPosts(final String... tags) {
        return client.get()
                .uri(builder -> builder
                        .scheme(scheme)
                        .host(apiUrl)
                        .port(port)
                        .path("index.php")
                        .queryParam("limit", "1000")
                        .queryParam("page", "dapi")
                        .queryParam("json", "1")
                        .queryParam("s", "post")
                        .queryParam("q", "index")
                        .queryParam("tags", String.join(",", tags))
                        .build())
                .retrieve()
                .bodyToFlux(Rule34Response.class)
                .flatMap(Rule34Response::toPosts);
    }
}
