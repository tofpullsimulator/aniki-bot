package org.eos.aniki.bot;

import java.util.List;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Listener object for {@link SlashCommand}s.
 *
 * @author Eos
 */
@Log4j2
@Component
public class SlashCommandListener {

    private final List<SlashCommand> commands;
    @Value("${discord.deniedIds}")
    private final List<String> deniedIds;

    /**
     * Create a new {@link SlashCommandListener} instance.
     *
     * @param client   The discord client.
     * @param commands The list of commands to listen for.
     */
    public SlashCommandListener(final GatewayDiscordClient client, final List<SlashCommand> commands,
                                final List<String> deniedIds) {
        this.commands = commands;
        this.deniedIds = deniedIds;
        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
        client.on(ChatInputAutoCompleteEvent.class, this::handle).subscribe();
    }

    /**
     * Handle an interaction event.
     *
     * @param event The interaction event to be handled.
     * @return The response of the handled command, or nothing if there is no command for the event.
     */
    Mono<Void> handle(final ChatInputInteractionEvent event) {
        var interaction = event.getInteraction();
        return interaction.getChannel()
                .filter(channel -> channel.getType() == Channel.Type.GUILD_TEXT)
                .cast(TextChannel.class)
                .flatMap(channel -> {
                    if (!channel.isNsfw()) {
                        return event.reply()
                                .withContent("Go be horny in the NSFW channels!")
                                .then();
                    }

                    if (interaction.getMember().isEmpty()) {
                        return event.reply()
                                .withEphemeral(true)
                                .withContent("You're not a real user!")
                                .then();
                    }

                    var member = interaction.getMember().get();
                    if (deniedIds.contains(member.getUserData().id().asString())) {
                        var content = String.format("You're denied access to the bot %s",
                                member.getUserData().username());
                        return event.reply()
                                .withContent(content)
                                .then();
                    }

                    return Flux.fromIterable(commands)
                            .filter(command -> command.getName().equals(event.getCommandName()))
                            .next()
                            .flatMap(command -> command.handle(event)
                                    .doOnError(e -> log.error("An error happened, {}", e.getMessage()))
                                    .onErrorResume(e -> command.handleError(event, e)));
                });
    }

    /**
     * Handle an autocomplete event.
     *
     * @param event The autocomplete event to be handled.
     * @return The response of the handled command, or nothing if there is no command for the event.
     */
    Mono<Void> handle(final ChatInputAutoCompleteEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .flatMap(command -> command.handle(event)
                        .doOnError(e -> log.error("An error happened, {}", e.getMessage())));
    }
}
