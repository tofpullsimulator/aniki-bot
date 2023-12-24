package org.eos.aniki.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import lombok.AllArgsConstructor;
import org.eos.aniki.bot.services.PostRepository;
import org.eos.aniki.bot.services.PostService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Main slash command for the bot.
 *
 * @author Eos
 */
@AllArgsConstructor
@Component
public class SlashCommand {

    /**
     * Value for the <pre>source</pre> option.
     */
    static final String SOURCE_OPTION = "source";
    /**
     * Value for the <pre>tags</pre> option.
     */
    static final String TAGS_OPTION = "tags";

    private final Map<String, PostRepository> repositories;
    private final PostService service;
    private final RandomGenerator random;

    /**
     * Get the name of the command.
     *
     * @return The name of the command.
     */
    public String getName() {
        return "aniki";
    }

    /**
     * Handle an interaction event.
     *
     * @param event The interaction event to be handled.
     * @return The result of the handled event.
     */
    public Mono<Void> handle(final ChatInputInteractionEvent event) {
        var tags = event.getOption("tags")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .map(it -> it.split(","))
                .orElse(new String[]{""});
        PostRepository repository = getPostRepository(event.getOption(SOURCE_OPTION));
        EmbedCreateSpec embed = service.getRandomPost(repository, tags);

        return event.reply().withEmbeds(embed);
    }

    /**
     * Handle the errors of the command.
     *
     * @param event The interaction event to be handled.
     * @param error The error to be handled.
     * @return The result of the handled event.
     */
    public Mono<Void> handleError(final ChatInputInteractionEvent event, Throwable error) {
        return event.reply()
                .withEphemeral(true)
                .withContent("Sorry, an error occurred while getting your random image");
    }

    /**
     * Handle an autocomplete event.
     *
     * @param event The autocomplete event to be handled.
     * @return The result of the handled event.
     */
    public Mono<Void> handle(final ChatInputAutoCompleteEvent event) {
        PostRepository repository = getPostRepository(event.getOption(SOURCE_OPTION));
        String focusedOption = event.getFocusedOption().getName();

        if (focusedOption.equals(SOURCE_OPTION)) {
            List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();
            for (String key : repositories.keySet()) {
                ApplicationCommandOptionChoiceData suggestion = ApplicationCommandOptionChoiceData.builder()
                        .name(key)
                        .value(key)
                        .build();
                suggestions.add(suggestion);
            }

            return event.respondWithSuggestions(suggestions);
        }

        if (focusedOption.equals(TAGS_OPTION)) {
            String keywords = event.getFocusedOption().getValue()
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .orElse("");
            List<ApplicationCommandOptionChoiceData> suggestions = service.getSuggestions(repository, keywords);

            return event.respondWithSuggestions(suggestions);
        }

        return Mono.empty();
    }

    private PostRepository getPostRepository(final Optional<ApplicationCommandInteractionOption> option) {
        var source = option.flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse(null);

        List<PostRepository> values = new ArrayList<>(repositories.values());
        PostRepository repository = values.get(random.nextInt(values.size()));
        if (source != null && !source.isEmpty() && repositories.containsKey(source)) {
            repository = repositories.get(source);
        }

        return repository;
    }
}
