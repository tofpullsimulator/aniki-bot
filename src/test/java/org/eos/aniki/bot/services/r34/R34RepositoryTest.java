package org.eos.aniki.bot.services.r34;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.PostRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * @author Eos
 */
@SpringBootTest(classes = {R34Config.class})
class R34RepositoryTest {

    private static MockWebServer server;

    @Autowired
    private R34Config config;
    private String baseUrl;
    private ObjectMapper mapper;
    private PostRepository repository;

    @BeforeAll
    static void setUpAll() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @BeforeEach
    void setUp() {
        baseUrl = String.format("http://localhost:%s", server.getPort());
        String cdnUrl = String.format("http://localhost:%s", server.getPort());
        WebClient client = config.client(baseUrl);

        mapper = new ObjectMapper();
        repository = new R34Repository(baseUrl, cdnUrl, "thumbnail", client);
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        server.shutdown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"nier"})
    @EmptySource
    void shouldGetTagsWithKeywords(final String keywords) throws IOException {
        R34Response.R34Tag tag1 = new R34Response.R34Tag(1, "value1", 1, 1, 1);
        R34Response.R34Tag tag2 = new R34Response.R34Tag(2, "value2", 2, 2, 2);
        List<R34Response.R34Tag> tags = List.of(tag1, tag2);

        server.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(tags))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getTags(keywords);
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertEquals("value1 (1)", it.name());
                    Assertions.assertEquals("value1", it.value());
                })
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldGetEmptyWhenErrorOccurredWhenGettingTags() {
        server.enqueue(new MockResponse()
                .setStatus("500")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getTags("nier");
        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void shouldGetPosts() throws IOException {
        R34Response.R34Post post1 = new R34Response.R34Post("posted1", 1, 0, 1, 1, 1, 1,
                Collections.emptyMap(), 123456, "created1");
        R34Response.R34Post post2 = new R34Response.R34Post("posted2", 2, 1, 2, 2, 2, 2,
                Collections.emptyMap(), 1234, "created2");
        R34Response response = new R34Response(List.of(post1, post2), 1);

        server.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getPosts("nier: automata");
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertEquals("ID: 123456", it.title());
                    Assertions.assertEquals(baseUrl + "/post/123456", it.url());
                    Assertions.assertEquals(baseUrl + "/posts/123/123456/123456.jpg", it.mediaUrl());
                    Assertions.assertEquals(Post.PostType.IMAGE, it.type());
                })
                .assertNext(it -> {
                    Assertions.assertEquals("ID: 1234", it.title());
                    Assertions.assertEquals(baseUrl + "/post/1234", it.url());
                    Assertions.assertEquals(baseUrl + "/posts/12/1234/1234.thumbnail.jpg", it.mediaUrl());
                    Assertions.assertEquals(Post.PostType.VIDEO, it.type());
                })
                .verifyComplete();
    }

    @Test
    void shouldGetEmptyWhenErrorOccurredWhenGettingPosts() {
        server.enqueue(new MockResponse()
                .setStatus("500")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getPosts("nier: automata");
        StepVerifier.create(mono).verifyComplete();
    }
}
