package org.eos.aniki.bot.services.r34;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.Tag;
import reactor.core.publisher.Flux;

/**
 * POJO class for the response for a r34.xyz/anime.r34 post response.
 *
 * @author Eos
 */
public record R34Response(
        List<R34Post> items,
        int cursor
) {

    /**
     * Create an empty <pre>r34Response</pre> instance.
     *
     * @return An empty <pre>R34Response</pre> instance.
     */
    public static R34Response empty() {
        return new R34Response(Collections.emptyList(), 0);
    }

    /**
     * POJO class for the response for a r34.xyz/anime.r34 tag.
     *
     * @author Eos
     */
    public record R34Tag(
            int id,
            String value,
            int popularity,
            int count,
            int type
    ) {

        /**
         * Create an empty <pre>R34Tag</pre> instance.
         *
         * @return An empty <pre>R34Tag</pre> instance.
         */
        public static R34Tag empty() {
            return new R34Tag(0, "", 0, 0, 0);
        }

        /**
         * Convert the <pre>R34Tag</pre> to a {@link Tag}.
         *
         * @return A list of converted tags.
         */
        public Flux<Tag> toTags() {
            return Flux.just(this)
                    .filter(it -> !it.equals(empty()))
                    .map(it -> {
                        String name = String.format("%s (%d)", it.value(), it.count());
                        return new Tag(name, it.value());
                    });
        }
    }

    /**
     * POJO class for the response for a r34.xyz/anime.r34 post.
     *
     * @author Eos
     */
    public record R34Post(
            String posted,
            int likes,
            int type,
            int status,
            int uploaderId,
            int width,
            int height,
            Map<String, List<Integer>> files,
            int id,
            String created
    ) {

    }

    /**
     * Convert the <pre>R34Post</pre> to a {@link Post}.
     *
     * @param baseUrl         The base url of the post.
     * @param cdnUrl          The cnd url of the post.
     * @param thumbnailPrefix The prefix of the thumbnail post.
     * @return A list of converted posts.
     */
    public Flux<Post> toPosts(final String baseUrl, final String cdnUrl, final String thumbnailPrefix) {
        return Flux.fromIterable(items).map(it -> {
            int type = it.type();
            Post.PostType postType = type == 1 ? Post.PostType.VIDEO : Post.PostType.IMAGE;

            int id = it.id();
            String prefix = String.join("", Arrays.asList(Integer.toString(id).split("")).subList(0, 3));
            if (Integer.toString(id).length() <= 5) {
                prefix = String.join("", Arrays.asList(Integer.toString(id).split("")).subList(0, 2));
            }

            String mediaUrl = String.format("%s/posts/%s/%d/%d.jpg", cdnUrl, prefix, id, id);
            if (postType == Post.PostType.VIDEO) {
                mediaUrl = String.format("%s/posts/%s/%d/%d.%s.jpg", cdnUrl, prefix, id, id, thumbnailPrefix);
            }

            String title = String.format("ID: %d", id);
            String url = String.format("%s/post/%d", baseUrl, id);

            return new Post(title, url, mediaUrl, postType);
        });
    }
}
