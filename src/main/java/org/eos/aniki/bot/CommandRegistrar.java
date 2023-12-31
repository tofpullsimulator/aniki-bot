package org.eos.aniki.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Registrar of commands based on the JSON files in <pre>resources/commands/</pre>
 *
 * @author Eos
 */
@AllArgsConstructor
@Log4j2
@Component
public class CommandRegistrar implements ApplicationRunner {

    private final RestClient restClient;
    private final Long guildId;

    /**
     * Creates commands from JSON files in <pre>resources/commands/</pre>. The commands are registered to the guild if
     * {@link BotConfiguration#getGuildId()} is set, otherwise they are set globally.
     *
     * @param arguments incoming application arguments
     * @throws IOException If something goes wrong while looking up the JSON files.
     */
    @Override
    public void run(final ApplicationArguments arguments) throws IOException {
        run(arguments, guildId);
    }

    /**
     * Creates commands from JSON files in <pre>resources/commands/</pre>. The commands are registered to the guild if
     * {@link BotConfiguration#getGuildId()} is set, otherwise they are set globally.
     *
     * @param arguments incoming application arguments
     * @param guildId The id of the discord server to create the commands for.
     * @throws IOException If something goes wrong while looking up the JSON files.
     */
    @SuppressWarnings("unused")
    void run(final ApplicationArguments arguments, final Long guildId) throws IOException {
        JacksonResources d4jMapper = JacksonResources.create();
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();

        ApplicationService applicationService = restClient.getApplicationService();
        @SuppressWarnings({"java:S2259", "DataFlowIssue"})
        long applicationId = restClient.getApplicationId().block();

        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);
            commands.add(request);
        }

        Flux<ApplicationCommandData> build;
        if (guildId != -1L) {
            build = applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guildId, commands)
                    .doOnNext(ignore -> log.info("Successfully registered Guild Commands"));
        } else {
            build = applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                    .doOnNext(ignore -> log.info("Successfully registered Global Commands"));
        }

        build.doOnError(e -> log.error("Failed to register global commands", e))
                .subscribe();
    }
}
