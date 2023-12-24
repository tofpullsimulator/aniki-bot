package org.eos.aniki.bot;

import java.util.Collections;
import java.util.Optional;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import discord4j.discordjson.json.ApplicationCommandInteractionResolvedData;
import org.eos.aniki.bot.services.PostRepository;
import org.eos.aniki.bot.services.PostService;
import org.eos.aniki.bot.services.ServiceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SlashCommand.class, ServiceConfig.class})
class SlashCommandTest {

    @Autowired
    @Qualifier("slashCommand")
    private SlashCommand command;

    @MockBean(name = "mockRepository")
    private PostRepository repository;
    @MockBean
    private PostService service;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GatewayDiscordClient gateway;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InteractionApplicationCommandCallbackReplyMono reply;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatInputInteractionEvent interactionEvent;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatInputAutoCompleteEvent autoCompleteEvent;

    @BeforeEach
    void setUp() {
        when(reply.withEmbeds(any(EmbedCreateSpec.class))).thenReturn(reply);
        when(reply.withEphemeral(anyBoolean())).thenReturn(reply);
        when(reply.withContent(anyString())).thenReturn(reply);
        when(reply.then()).thenReturn(Mono.empty());
        when(interactionEvent.reply()).thenReturn(reply);
        when(autoCompleteEvent.respondWithSuggestions(anyList())).thenReturn(Mono.empty());
    }

    @Test
    void shouldGetTheNameOfTheCommand() {
        Assertions.assertEquals("aniki", command.getName());
    }

    @Test
    void shouldHandleInteractionEventWithARandomRepository() {
        var option = createOption(SlashCommand.TAGS_OPTION, "nier");
        when(interactionEvent.getOption(SlashCommand.TAGS_OPTION)).thenReturn(Optional.of(option));
        var embed = EmbedCreateSpec.builder().build();
        when(service.getRandomPost(repository, "nier")).thenReturn(embed);

        var mono = command.handle(interactionEvent).then();
        StepVerifier.create(mono).verifyComplete();
        verify(reply).withEmbeds(any(EmbedCreateSpec.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mockRepository", "invalid"})
    @EmptySource
    void shouldHandleInteractionEventWithAGivenRepository(final String source) {
        var sourceOption = createOption(SlashCommand.SOURCE_OPTION, source);
        when(interactionEvent.getOption(SlashCommand.SOURCE_OPTION)).thenReturn(Optional.of(sourceOption));

        var tagsOption = createOption(SlashCommand.TAGS_OPTION, "nier");
        when(interactionEvent.getOption(SlashCommand.TAGS_OPTION)).thenReturn(Optional.of(tagsOption));

        var embed = EmbedCreateSpec.builder().build();
        when(service.getRandomPost(repository, "nier")).thenReturn(embed);

        var mono = command.handle(interactionEvent).then();
        StepVerifier.create(mono).verifyComplete();
        verify(reply).withEmbeds(any(EmbedCreateSpec.class));
    }

    @Test
    void shouldHandleError() {
        var mono = command.handleError(interactionEvent, new Throwable("This is a test"))
                .then();
        StepVerifier.create(mono).verifyComplete();

        verify(reply).withEphemeral(true);
        verify(reply).withContent("Sorry, an error occurred while getting your random image");
    }

    @Test
    void shouldHandleAutoCompleteEventOnSource() {
        var option = createOption(SlashCommand.SOURCE_OPTION, "mock");
        when(autoCompleteEvent.getFocusedOption()).thenReturn(option);

        var mono = command.handle(autoCompleteEvent);
        StepVerifier.create(mono).verifyComplete();
        verify(autoCompleteEvent).respondWithSuggestions(anyList());
    }

    @Test
    void shouldHandleAutoCompleteEventOnTags() {
        var option = createOption(SlashCommand.TAGS_OPTION, "nier");
        when(autoCompleteEvent.getFocusedOption()).thenReturn(option);
        when(service.getSuggestions(repository, "nier")).thenReturn(Collections.emptyList());

        var mono = command.handle(autoCompleteEvent);
        StepVerifier.create(mono).verifyComplete();
        verify(autoCompleteEvent).respondWithSuggestions(anyList());
    }

    @Test
    void shouldHandleAutoCompleteEventOnInvalidOption() {
        var option = createOption("invalid", "nier");
        when(autoCompleteEvent.getFocusedOption()).thenReturn(option);

        var mono = command.handle(autoCompleteEvent);
        StepVerifier.create(mono).verifyComplete();
        verify(autoCompleteEvent, times(0)).respondWithSuggestions(anyList());
    }

    private ApplicationCommandInteractionOption createOption(final String name, final String value) {
        var data = ApplicationCommandInteractionOptionData.builder()
                .type(3)
                .name(name)
                .value(value)
                .focused(true)
                .build();
        var resolved = ApplicationCommandInteractionResolvedData.builder().build();

        return new ApplicationCommandInteractionOption(gateway, data, 1L, resolved);
    }
}
