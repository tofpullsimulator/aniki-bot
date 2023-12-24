package org.eos.aniki.bot.services.r34;

import java.util.List;

/**
 * Body for fetching r34.xyz/anime.r34 posts.
 *
 * @author Eos
 */
public record R34Body(
        int blacklistType,
        List<String> includeTags,
        int sortOrder,
        int status,
        int take
) {

    /**
     * Wrapper to add default values, except to <pre>tags</pre>.
     *
     * @param tags The tags for the body.
     */
    public R34Body(final String... tags) {
        this(1, List.of(tags), 1, 2, 1000);
    }
}
