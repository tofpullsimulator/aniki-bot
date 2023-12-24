package org.eos.aniki.bot.services.gelbooru;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.Tag;
import reactor.core.publisher.Flux;

/**
 * POJO class for the response for a Gelbooru post response.
 *
 * @author Eos
 */
public record GelbooruResponse(
        List<GelbooruPost> post
) {

    /**
     * Create an empty <pre>GelbooruResponse</pre> instance.
     *
     * @return An empty <pre>GelbooruResponse</pre> instance.
     */
    public static GelbooruResponse empty() {
        return new GelbooruResponse(Collections.emptyList());
    }

    /**
     * POJO class for the response for a Gelbooru tag.
     *
     * @author Eos
     */
    public record GelbooruTag(
            @JsonProperty("type")
            String type,
            @JsonProperty("label")
            String label,
            @JsonProperty("value")
            String value,
            @JsonProperty("post_count")
            String postCount,
            @JsonProperty("category")
            String category
    ) {

        /**
         * Create an empty <pre>GelbooruTag</pre> instance.
         *
         * @return An empty <pre>GelbooruTag</pre> instance.
         */
        public static GelbooruTag empty() {
            return new GelbooruTag("", "", "", "", "");
        }

        /**
         * Convert the <pre>GelbooruTag</pre> to a {@link Tag}.
         *
         * @return A list of converted tags.
         */
        public Flux<Tag> toTags() {
            return Flux.just(this)
                    .filter(it -> !it.equals(empty()))
                    .map(it -> {
                        String name = String.format("%s (%s)", it.label(), it.postCount());
                        return new Tag(name, it.value());
                    });
        }
    }

    /**
     * POJO class for the response for a Gelbooru post.
     *
     * @author Eos
     */
    public record GelbooruPost(
            @JsonProperty("id")
            int id,
            @JsonProperty("parent_id")
            int parentId,
            @JsonProperty("creator_id")
            int creatorId,
            @JsonProperty("score")
            int score,
            @JsonProperty("height")
            int height,
            @JsonProperty("width")
            int width,
            @JsonProperty("md5")
            String md5,
            @JsonProperty("directory")
            String directory,
            @JsonProperty("image")
            String image,
            @JsonProperty("rating")
            String rating,
            @JsonProperty("source")
            String source,
            @JsonProperty("change")
            int change,
            @JsonProperty("owner")
            String owner,
            @JsonProperty("sample")
            int sample,
            @JsonProperty("preview_height")
            int previewHeight,
            @JsonProperty("preview_width")
            int previewWidth,
            @JsonProperty("tags")
            String tags,
            @JsonProperty("title")
            String title,
            @JsonProperty("has_notes")
            String hasNotes,
            @JsonProperty("has_comments")
            String hasComments,
            @JsonProperty("file_url")
            String fileUrl,
            @JsonProperty("preview_url")
            String previewUrl,
            @JsonProperty("sample_url")
            String sampleUrl,
            @JsonProperty("sample_height")
            int sampleHeight,
            @JsonProperty("sample_width")
            int sampleWidth,
            @JsonProperty("status")
            String status,
            @JsonProperty("post_locked")
            int postLocked,
            @JsonProperty("has_children")
            String hasChildren
    ) {

        /**
         * Wrapper to add default values, except to <pre>id, fileUrl, previewUrl</pre>.
         *
         * @param id         The id for the post.
         * @param fileUrl    The file url for the post.
         * @param previewUrl The preview url for the post.
         */
        public GelbooruPost(final int id, final String fileUrl, final String previewUrl) {
            this(id, 0, 0, 0, 0, 0, "", "", "", "", "", 0, "", 0, 0, 0, "", "", "", "", fileUrl, previewUrl,
                    "", 0, 0, "", 0, "");
        }
    }

    /**
     * Convert the <pre>GelbooruPost</pre> to a {@link Post}.
     *
     * @return A list of converted posts.
     */
    public Flux<Post> toPosts() {
        return Flux.fromIterable(post).map(it -> {
            Post.PostType postType = it.fileUrl().endsWith(".mp4") ? Post.PostType.VIDEO : Post.PostType.IMAGE;

            String mediaUrl = it.fileUrl();
            if (postType == Post.PostType.VIDEO) {
                mediaUrl = it.previewUrl();
            }

            int id = it.id();
            String title = String.format("ID: %d", id);
            String url = String.format("https://gelbooru.com/index.php?page=post&s=view&id=%s", id);

            return new Post(title, url, mediaUrl, postType);
        });
    }
}
