package org.eos.aniki.bot.services.r34;

import org.eos.aniki.bot.services.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

/**
 * @author Eos
 */
@Tag("integration-tests")
@SpringBootTest(classes = {R34Config.class})
class AnimeR34RepositoryIntegrationTest {

    @Autowired
    @Qualifier("animer34")
    private PostRepository repository;

    @ParameterizedTest
    @ValueSource(strings = {"nier"})
    @EmptySource
    void shouldGetTagsWithKeywords(final String keywords) {
        var mono = repository.getTags(keywords);
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertNotNull(it.name());
                    Assertions.assertNotNull(it.value());
                })
                .expectNextCount(19)
                .verifyComplete();
    }

    @Test
    void shouldGetPosts() {
        var mono = repository.getPosts("nier (series)");
        StepVerifier.create(mono)
                .assertNext(it -> Assertions.assertNotNull(it.mediaUrl()))
                .expectNextCount(999)
                .verifyComplete();
    }
}
