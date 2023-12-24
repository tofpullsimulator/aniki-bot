package org.eos.aniki.bot.services;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * An interface to talk to an API service for fetching posts & tags.
 *
 * @author Eos
 */
@Repository
public interface PostRepository {

    /**
     * Get tags from a given API service.
     *
     * @param keywords The keywords to search with.
     * @return A list of found tags.
     */
    Flux<Tag> getTags(final String keywords);

    /**
     * Get posts from a given API service.
     *
     * @param tags The tags to search with.
     * @return A list of found posts.
     */
    Flux<Post> getPosts(final String... tags);
}
