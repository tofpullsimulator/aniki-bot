package org.eos.aniki.bot;

import java.io.IOException;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Eos
 */
@SuppressWarnings("SpringBootApplicationProperties")
@SpringBootTest(
        classes = {CommandRegistrar.class, BotConfiguration.class},
        properties = {"discord.guildId=-1"}
)
class CommandRegistrarTest {

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private GatewayDiscordClient client;
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;
    @MockBean
    private ApplicationService applicationService;

    @Autowired
    private CommandRegistrar registrar;

    private ApplicationCommandData data;

    @BeforeEach
    void setUp() {
        data = ApplicationCommandData.builder()
                .id(1L)
                .applicationId(1L)
                .version(1L)
                .name("testing")
                .description("This is a test")
                .build();

        when(client.getRestClient()).thenReturn(restClient);
        when(restClient.getApplicationService()).thenReturn(applicationService);
        when(restClient.getApplicationId()).thenReturn(Mono.just(1L));
    }

    @Test
    void shouldRegistrarGlobalCommandsDefault() throws IOException {
        when(applicationService.bulkOverwriteGlobalApplicationCommand(anyLong(), anyList()))
                .thenReturn(Flux.just(data));
        registrar.run(null);
        verify(applicationService).bulkOverwriteGlobalApplicationCommand(eq(1L), anyList());
    }

    @Test
    void shouldRegistrarGlobalCommands() throws IOException {
        when(applicationService.bulkOverwriteGlobalApplicationCommand(anyLong(), anyList()))
                .thenReturn(Flux.just(data));
        registrar.run(null, -1L);
        verify(applicationService).bulkOverwriteGlobalApplicationCommand(eq(1L), anyList());
    }

    @Test
    void shouldRegistrarGuildCommands() throws IOException {
        when(applicationService.bulkOverwriteGuildApplicationCommand(anyLong(), anyLong(), anyList()))
                .thenReturn(Flux.just(data));
        registrar.run(null, 1L);
        verify(applicationService).bulkOverwriteGuildApplicationCommand(eq(1L), eq(1L), anyList());
    }

    @Test
    void shouldRegistrarCommandsException() throws IOException {
        when(applicationService.bulkOverwriteGuildApplicationCommand(anyLong(), anyLong(), anyList()))
                .thenReturn(Flux.error(new Exception("This is a test exception")));
        registrar.run(null, 1L);
        verify(applicationService).bulkOverwriteGuildApplicationCommand(eq(1L), eq(1L), anyList());
    }
}
