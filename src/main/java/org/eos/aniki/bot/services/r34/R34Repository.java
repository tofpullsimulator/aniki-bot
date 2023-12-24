package org.eos.aniki.bot.services.r34;

import lombok.AllArgsConstructor;
import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.PostRepository;
import org.eos.aniki.bot.services.Tag;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository to fetch posts and tags via the r34.xyz/anime.r34 API service with.
 *
 * @author Eos
 */
@AllArgsConstructor
public class R34Repository implements PostRepository {

    private final String baseUrl;
    private final String cdnUrl;
    private final WebClient client;

    /**
     * {@inheritDoc}
     */
    public Flux<Tag> getTags(final String keywords) {
        WebClient.RequestHeadersSpec<?> request = client.get()
                .uri("/api/tag/search/" + keywords);
        if (keywords.isEmpty()) {
            request = client.get().uri("/api/tag/");
        }

        return request.retrieve()
                .bodyToFlux(R34Response.R34Tag.class)
                .flatMap(R34Response.R34Tag::toTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Post> getPosts(final String... tags) {
        var data = new R34Body(tags);
        return client.post()
                .uri("/api/post/search/root")
                .body(Mono.just(data), R34Body.class)
                .retrieve()
                .bodyToMono(R34Response.class)
                .flatMapMany(it -> it.toPosts(baseUrl, cdnUrl));
    }
}
