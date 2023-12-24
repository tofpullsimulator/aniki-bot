package org.eos.aniki.bot.services.rule34;

import org.eos.aniki.bot.services.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

/**
 * @author Eos
 */
@Tag("integration-tests")
@SpringBootTest(classes = {Rule34Config.class})
class Rule34RepositoryIntegrationTest {

    @Autowired
    @Qualifier("rule34")
    private PostRepository repository;

    @Test
    void shouldGetTagsWithKeywords() {
        var mono = repository.getTags("nier");
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertNotNull(it.name());
                    Assertions.assertNotNull(it.value());
                })
                .expectNextCount(8)
                .verifyComplete();
    }

    @Test
    void shouldGetPosts() {
        var mono = repository.getPosts("nier_(series)");
        StepVerifier.create(mono)
                .assertNext(it -> Assertions.assertNotNull(it.mediaUrl()))
                .expectNextCount(999)
                .verifyComplete();
    }
}
