package org.eos.aniki.bot;

import java.util.Optional;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Eos
 */
@SuppressWarnings("SpringBootApplicationProperties")
@SpringBootTest(
        classes = {SlashCommandListener.class, MockCommands.class},
        properties = {"discord.deniedIds=2"}

)
class SlashCommandListenerTest {

    @Autowired
    private SlashCommandListener listener;

    @SuppressWarnings("unused")
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private GatewayDiscordClient client;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InteractionApplicationCommandCallbackReplyMono reply;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Interaction interaction;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TextChannel channel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatInputInteractionEvent interactionEvent;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatInputAutoCompleteEvent autoCompleteEvent;

    @BeforeEach
    void setUp() {
        when(channel.getType()).thenReturn(Channel.Type.GUILD_TEXT);
        when(interaction.getChannel()).thenReturn(Mono.just(channel));
        when(reply.withEphemeral(anyBoolean())).thenReturn(reply);
        when(reply.withContent(anyString())).thenReturn(reply);
        when(reply.then()).thenReturn(Mono.empty());
        when(interactionEvent.reply()).thenReturn(reply);
        when(interactionEvent.getInteraction()).thenReturn(interaction);
        when(autoCompleteEvent.respondWithSuggestions(anyList())).thenReturn(Mono.empty());
    }

    @Test
    void shouldHandleInteractionEventWhenNotInATextChannel() {
        when(channel.getType()).thenReturn(Channel.Type.GUILD_VOICE);
        when(interactionEvent.getCommandName()).thenReturn("mock");

        var mono = listener.handle(interactionEvent).then().then();
        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void shouldHandleInteractionEventWhenNotInAnNSFWChannel() {
        when(channel.isNsfw()).thenReturn(false);
        when(interactionEvent.getCommandName()).thenReturn("mock");

        var mono = listener.handle(interactionEvent).then().then();
        StepVerifier.create(mono).verifyComplete();

        verify(reply).withContent("Go be horny in the NSFW channels!");
    }

    @Test
    void shouldHandleInteractionEventWithoutAMember() {
        when(channel.isNsfw()).thenReturn(true);
        when(interaction.getMember()).thenReturn(Optional.empty());
        when(interactionEvent.getCommandName()).thenReturn("mock");

        var mono = listener.handle(interactionEvent).then();
        StepVerifier.create(mono).verifyComplete();

        verify(reply).withContent("You're not a real user!");
    }

    @Test
    void shouldHandleInteractionEventWithADisallowedMember() {
        when(channel.isNsfw()).thenReturn(true);
        var member = createMember("2");
        when(interaction.getMember()).thenReturn(Optional.of(member));

        when(interactionEvent.getCommandName()).thenReturn("mock");
        var mono = listener.handle(interactionEvent).then();
        StepVerifier.create(mono).verifyComplete();

        verify(reply).withContent("You're denied access to the bot Eos");
    }

    @Test
    void shouldHandleInteractionEvent() {
        when(channel.isNsfw()).thenReturn(true);
        var member = createMember("1");
        when(interaction.getMember()).thenReturn(Optional.of(member));

        when(interactionEvent.getCommandName()).thenReturn("mock");
        var mono = listener.handle(interactionEvent).then();
        StepVerifier.create(mono).verifyComplete();

        verify(reply).withContent("mock");
    }

    @Test
    void shouldFailInteractionEvent() {
        when(channel.isNsfw()).thenReturn(true);
        var member = createMember("1");
        when(interaction.getMember()).thenReturn(Optional.of(member));

        when(interactionEvent.getCommandName()).thenReturn("failing");
        var mono = listener.handle(interactionEvent).then();
        StepVerifier.create(mono).verifyError();
    }

    @Test
    void shouldHandleAutoCompleteEvent() {
        when(autoCompleteEvent.getCommandName()).thenReturn("mock");
        var mono = listener.handle(autoCompleteEvent).then();
        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    void shouldFailAutoCompleteEvent() {
        when(autoCompleteEvent.getCommandName()).thenReturn("failing");
        var mono = listener.handle(autoCompleteEvent).then();
        StepVerifier.create(mono).verifyError();
    }

    private Member createMember(final String id) {
        @SuppressWarnings("deprecation")
        var memberData = MemberData.builder()
                .mute(false)
                .deaf(false)
                .user(UserData.builder()
                        .id(id)
                        .username("Eos")
                        .discriminator("Eos")
                        .build())
                .build();
        return new Member(client, memberData, 1L);
    }
}
