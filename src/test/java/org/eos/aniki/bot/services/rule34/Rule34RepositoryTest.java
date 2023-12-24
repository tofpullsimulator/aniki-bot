package org.eos.aniki.bot.services.rule34;

import java.io.IOException;
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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * @author Eos
 */
@SpringBootTest(classes = {Rule34Config.class})
class Rule34RepositoryTest {

    private static MockWebServer server;

    @Autowired
    private Rule34Config config;
    private ObjectMapper mapper;
    private PostRepository repository;

    @BeforeAll
    static void setUpAll() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = "localhost";
        WebClient client = config.client();

        mapper = new ObjectMapper();
        repository = new Rule34Repository("http", baseUrl, baseUrl, server.getPort(), client);
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldGetTagsWithKeywords() throws IOException {
        Rule34Response.Rule34Tag tag1 = new Rule34Response.Rule34Tag("label1", "value1", "copyright");
        Rule34Response.Rule34Tag tag2 = new Rule34Response.Rule34Tag("label2", "value2", "general");
        List<Rule34Response.Rule34Tag> tags = List.of(tag1, tag2);

        server.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(tags))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getTags("nier");
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertEquals("label1", it.name());
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

    @ParameterizedTest
    @NullAndEmptySource
    void shouldGetTagsWithoutKeywords(final String keywords) {
        var mono = repository.getTags(keywords);
        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void shouldGetPosts() throws IOException {
        Rule34Response post1 = new Rule34Response(1, "fileUrl1.jpg", "previewUrl1.jpg");
        Rule34Response post2 = new Rule34Response(2, "fileUrl2.mp4", "previewUrl2.mp4");
        List<Rule34Response> response = List.of(post1, post2);

        server.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getPosts("nier: automata");
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertEquals("ID: 1", it.title());
                    Assertions.assertEquals("https://rule34.xxx/index.php?page=post&s=view&id=1", it.url());
                    Assertions.assertEquals("fileUrl1.jpg", it.mediaUrl());
                    Assertions.assertEquals(Post.PostType.IMAGE, it.type());
                })
                .assertNext(it -> {
                    Assertions.assertEquals("ID: 2", it.title());
                    Assertions.assertEquals("https://rule34.xxx/index.php?page=post&s=view&id=2", it.url());
                    Assertions.assertEquals("previewUrl2.mp4", it.mediaUrl());
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
