package org.eos.aniki.bot.services.rule34;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eos.aniki.bot.services.Post;
import org.eos.aniki.bot.services.Tag;
import reactor.core.publisher.Flux;

/**
 * POJO class for the response for a Rule34 post response.
 *
 * @author Eos
 */
public record Rule34Response(
        @JsonProperty("id")
        int id,
        @JsonProperty("parent_id")
        int parentId,
        @JsonProperty("score")
        int score,
        @JsonProperty("height")
        int height,
        @JsonProperty("width")
        int width,
        @JsonProperty("hash")
        String hash,
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
        boolean sample,
        @JsonProperty("tags")
        String tags,
        @JsonProperty("has_notes")
        boolean hasNotes,
        @JsonProperty("file_url")
        String fileUrl,
        @JsonProperty("preview_url")
        String previewUrl,
        @JsonProperty("sample_url")
        String sampleUrl,
        @JsonProperty("status")
        String status,
        @JsonProperty("comment_count")
        int hasChildren
) {

    /**
     * Wrapper to add default values, except to <pre>id, fileUrl, previewUrl</pre>.
     *
     * @param id         The id for the post.
     * @param fileUrl    The file url for the post.
     * @param previewUrl The preview url for the post.
     */
    public Rule34Response(final int id, final String fileUrl, final String previewUrl) {
        this(id, 0, 0, 0, 0, "", "", "", "", "", 0, "", false, "", false, fileUrl, previewUrl, "", "", 0);
    }

    /**
     * POJO class for the response for a Rule34 tag.
     *
     * @author Eos
     */
    public record Rule34Tag(
            @JsonProperty("label")
            String label,
            @JsonProperty("value")
            String value,
            @JsonProperty("type")
            String type
    ) {

        /**
         * Convert the <pre>Rule34Tag</pre> to a {@link Tag}.
         *
         * @return A list of converted tags.
         */
        public Flux<Tag> toTags() {
            return Flux.just(this).map(it -> new Tag(it.label(), it.value()));
        }
    }

    /**
     * Convert the <pre>Rule34Post</pre> to a {@link Post}.
     *
     * @return A list of converted posts.
     */
    public Flux<Post> toPosts() {
        return Flux.just(this).map(it -> {
            Post.PostType postType = it.fileUrl().endsWith(".mp4") ? Post.PostType.VIDEO : Post.PostType.IMAGE;

            String mediaUrl = it.fileUrl();
            if (postType == Post.PostType.VIDEO) {
                mediaUrl = it.previewUrl();
            }

            int id = it.id();
            String title = String.format("ID: %d", id);
            String url = String.format("https://rule34.xxx/index.php?page=post&s=view&id=%s", id);

            return new Post(title, url, mediaUrl, postType);
        });
    }
}
