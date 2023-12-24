package org.eos.aniki.bot.services.gelbooru;

import lombok.AllArgsConstructor;
import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.PostRepository;
import org.eos.aniki.bot.services.Tag;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Repository to fetch posts and tags via the Gelbooru API service with.
 *
 * @author Eos
 */
@AllArgsConstructor
public class GelbooruRepository implements PostRepository {

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
                        .path("/index.php")
                        .queryParam("page", "autocomplete2")
                        .queryParam("term", keywords)
                        .queryParam("type", "tag_query")
                        .queryParam("limit", "20")
                        .build())
                .retrieve()
                .bodyToFlux(GelbooruResponse.GelbooruTag.class)
                .flatMap(GelbooruResponse.GelbooruTag::toTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Post> getPosts(final String... tags) {
        return client.get()
                .uri(builder -> builder
                        .path("/index.php")
                        .queryParam("limit", "100")
                        .queryParam("page", "dapi")
                        .queryParam("json", "1")
                        .queryParam("s", "post")
                        .queryParam("q", "index")
                        .queryParam("tags", String.join(",", tags))
                        .build())
                .retrieve()
                .bodyToMono(GelbooruResponse.class)
                .flatMapMany(GelbooruResponse::toPosts);
    }
}
