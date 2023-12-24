package org.eos.aniki.bot.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Eos
 */
@SuppressWarnings("SpringBootApplicationProperties")
@SpringBootTest(
        classes = {PostService.class, ServiceConfig.class},
        properties = {"discord.disallowedTags=furry,genshin impact"}
)
class PostServiceTest {

    @MockBean
    private PostRepository repository;

    @Autowired
    private PostService service;

    @Test
    void shouldGetSuggestions() {
        Flux<Tag> tags = Flux.just(new Tag("name (1234)", "value"));
        when(repository.getTags("nier")).thenReturn(tags);

        var result = service.getSuggestions(repository, "nier");
        Assertions.assertFalse(result.isEmpty());

        var data = result.get(0);
        Assertions.assertEquals("name (1234)", data.name());
        Assertions.assertEquals("value", data.value());
    }

    @Test
    void shouldGetEmptySuggestionsOnEmptyTags() {
        Flux<Tag> tags = Flux.empty();
        when(repository.getTags("nier")).thenReturn(tags);

        var result = service.getSuggestions(repository, "nier");
        Assertions.assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nier (1234)"})
    @EmptySource
    void shouldGetARandomImageWithATag(final String tags) {
        Flux<Post> posts = Flux.just(new Post("ID: 1", "url", "image", Post.PostType.IMAGE));
        when(repository.getPosts(anyString())).thenReturn(posts);

        var result = service.getRandomPost(repository, tags);

        Assertions.assertEquals("ID: 1", result.title().get());
        Assertions.assertEquals("url", result.url().get());
        String description = tags.isEmpty() ? "Tags used: none" : "Tags used: nier";
        Assertions.assertEquals(description, result.description().get());
        Assertions.assertEquals("image", result.image().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nier (1234)"})
    @EmptySource
    void shouldGetARandomVideoWithATag(final String tags) {
        Flux<Post> posts = Flux.just(new Post("ID: 1", "url", "thumbnail", Post.PostType.VIDEO));
        when(repository.getPosts(anyString())).thenReturn(posts);

        var result = service.getRandomPost(repository, tags);

        Assertions.assertEquals("ID: 1", result.title().get());
        Assertions.assertEquals("url", result.url().get());
        String description = tags.isEmpty() ? "Tags used: none" : "Tags used: nier";
        Assertions.assertEquals(description, result.description().get());
        Assertions.assertEquals("thumbnail", result.thumbnail().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"furry", "genshin impact"})
    void shouldGetNoPostWhenDisallowedTagsAreUsed(final String tags) {
        var result = service.getRandomPost(repository, tags);
        String description = "Used disallowed tags: " + tags;
        Assertions.assertEquals(description, result.description().get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nier (1234)"})
    @EmptySource
    void shouldGetNoResultsIfPostsAreEmpty(final String tags) {
        Flux<Post> posts = Flux.empty();
        when(repository.getPosts(anyString())).thenReturn(posts);

        var result = service.getRandomPost(repository, tags);
        String description = tags.isEmpty() ? "No posts found for tags: none" : "No posts found for tags: nier";
        Assertions.assertEquals(description, result.description().get());
    }
}
