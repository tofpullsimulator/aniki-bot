package org.eos.aniki.bot.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for fetching posts and tags with.
 *
 * @author Eos
 */
@AllArgsConstructor
@Service
public class PostService {

    private static final Pattern PATTERN = Pattern.compile(" \\(\\d+\\)");

    @Value("${discord.disallowedTags:}")
    private final List<String> disallowedTags;
    private final RandomGenerator random;

    /**
     * Fetch a list of tags to search with.
     *
     * @param repository The repository to fetch a list of posts from.
     * @param keywords   The keywords to search for tags.
     * @return A list of tags to search with in the form of {@link ApplicationCommandOptionChoiceData}.
     */
    public List<ApplicationCommandOptionChoiceData> getSuggestions(final PostRepository repository,
                                                                   final String keywords) {
        var results = repository.getTags(keywords).collectList().block();
        //noinspection DataFlowIssue
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();
        for (Tag tag : results) {
            var choice = ApplicationCommandOptionChoiceData.builder()
                    .name(tag.name())
                    .value(tag.value())
                    .build();
            suggestions.add(choice);
        }

        return suggestions;
    }

    /**
     * Fetch a random post from the repository with the given tags.
     *
     * @param repository The repository to fetch a list of posts from.
     * @param tags       The tags to search for posts.
     * @return A random post in the form of {@link EmbedCreateSpec}.
     */
    public EmbedCreateSpec getRandomPost(final PostRepository repository, final String... tags) {
        String[] clearedTags = Arrays.stream(tags).map(tag -> {
            Matcher matcher = PATTERN.matcher(tag);
            return matcher.replaceAll("");
        }).toArray(String[]::new);

        boolean containsDisallowed = Arrays.stream(clearedTags).anyMatch(disallowedTags::contains);
        if (containsDisallowed) {
            String description = getDescription("Used disallowed tags: %s", clearedTags);
            return EmbedCreateSpec.builder()
                    .description(description)
                    .build();
        }

        var results = repository.getPosts(clearedTags).collectList().block();
        //noinspection DataFlowIssue
        if (results.isEmpty()) {
            String description = getDescription("No posts found for tags: %s", clearedTags);
            return EmbedCreateSpec.builder()
                    .description(description)
                    .build();
        }

        String description = getDescription("Tags used: %s", clearedTags);
        Post post = results.get(random.nextInt(results.size()));
        if (Post.PostType.VIDEO.equals(post.type())) {
            return EmbedCreateSpec.builder()
                    .title(post.title())
                    .url(post.url())
                    .description(description)
                    .thumbnail(post.mediaUrl())
                    .build();
        }

        return EmbedCreateSpec.builder()
                .title(post.title())
                .url(post.url())
                .description(description)
                .image(post.mediaUrl())
                .build();
    }

    private String getDescription(final String message, final String... tags) {
        boolean isNotEmpty = Stream.of(tags).anyMatch(it -> !it.isEmpty());
        if (isNotEmpty) {
            return String.format(message, String.join(", ", tags));
        }

        return String.format(message, "none");
    }
}
