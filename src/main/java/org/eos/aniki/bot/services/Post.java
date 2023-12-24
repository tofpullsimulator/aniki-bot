package org.eos.aniki.bot.services;

/**
 * POJO class representing a post to search for.
 *
 * @author Eos
 */
public record Post(String title, String url, String mediaUrl, PostType type) {

    /**
     * Possible types for posts.
     *
     * @author Eos
     */
    public enum PostType {
        IMAGE, VIDEO
    }
}
