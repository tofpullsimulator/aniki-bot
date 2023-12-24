package org.eos.aniki.bot;

import java.util.Collections;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author Eos
 */
@Component
class MockCommands {

    /**
     * @author Eos
     */
    @Component
    public static class MockCommand extends SlashCommand {

        public MockCommand() {
            super(null, null, null);
        }

        @Override
        public String getName() {
            return "mock";
        }

        @Override
        public Mono<Void> handle(final ChatInputInteractionEvent event) {
            return event.reply().withContent("mock").then();
        }

        @Override
        public Mono<Void> handleError(final ChatInputInteractionEvent event, final Throwable error) {
            return event.reply().withContent("mock").then();
        }

        @Override
        public Mono<Void> handle(final ChatInputAutoCompleteEvent event) {
            return event.respondWithSuggestions(Collections.emptyList());
        }
    }

    /**
     * @author Eos
     */
    @Component
    public static class MockFailingCommand extends SlashCommand {

        public MockFailingCommand() {
            super(null, null, null);
        }

        @Override
        public String getName() {
            return "failing";
        }

        @Override
        public Mono<Void> handle(final ChatInputInteractionEvent event) {
            return Mono.error(new Exception("Failing handling interaction event")).then();
        }

        @Override
        public Mono<Void> handle(final ChatInputAutoCompleteEvent event) {
            return Mono.error(new Exception("Failing handling autocomplete event")).then();
        }

        @Override
        public Mono<Void> handleError(final ChatInputInteractionEvent event, final Throwable error) {
            return Mono.error(new Exception("Failing handling error")).then();
        }
    }
}
