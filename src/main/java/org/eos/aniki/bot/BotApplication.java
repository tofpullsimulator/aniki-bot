package org.eos.aniki.bot;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main for the bot.
 *
 * @author Eos
 */
@Generated
@SpringBootApplication
public class BotApplication {

    /**
     * Start the bot.
     *
     * @param args The arguments passed to the bot.
     */
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
