package org.eos.aniki.bot.services.gelbooru;

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
@SpringBootTest(classes = {GelbooruConfig.class})
class GelbooruRepositoryTest {

    private static MockWebServer server;

    @Autowired
    private GelbooruConfig config;
    private ObjectMapper mapper;
    private PostRepository repository;

    @BeforeAll
    static void setUpAll() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = String.format("http://localhost:%s", server.getPort());
        WebClient client = config.client(baseUrl);

        mapper = new ObjectMapper();
        repository = new GelbooruRepository(client);
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldGetTagsWithKeywords() throws IOException {
        GelbooruResponse.GelbooruTag tag1 = new GelbooruResponse.GelbooruTag("1", "value1", "value1", "1", "1");
        GelbooruResponse.GelbooruTag tag2 = new GelbooruResponse.GelbooruTag("2", "value2", "value2", "2", "2");
        List<GelbooruResponse.GelbooruTag> tags = List.of(tag1, tag2);

        server.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(tags))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getTags("nier");
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

    @ParameterizedTest
    @NullAndEmptySource
    void shouldGetTagsWithoutKeywords(final String keywords) {
        var mono = repository.getTags(keywords);
        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void shouldGetPosts() throws IOException {
        GelbooruResponse.GelbooruPost post1 = new GelbooruResponse.GelbooruPost(1, "fileUrl1.jpg", "previewUrl1.jpg");
        GelbooruResponse.GelbooruPost post2 = new GelbooruResponse.GelbooruPost(2, "fileUrl2.mp4", "previewUrl2.mp4");
        GelbooruResponse response = new GelbooruResponse(List.of(post1, post2));

        server.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        var mono = repository.getPosts("nier: automata");
        StepVerifier.create(mono)
                .assertNext(it -> {
                    Assertions.assertEquals("ID: 1", it.title());
                    Assertions.assertEquals("https://gelbooru.com/index.php?page=post&s=view&id=1", it.url());
                    Assertions.assertEquals("fileUrl1.jpg", it.mediaUrl());
                    Assertions.assertEquals(Post.PostType.IMAGE, it.type());
                })
                .assertNext(it -> {
                    Assertions.assertEquals("ID: 2", it.title());
                    Assertions.assertEquals("https://gelbooru.com/index.php?page=post&s=view&id=2", it.url());
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
