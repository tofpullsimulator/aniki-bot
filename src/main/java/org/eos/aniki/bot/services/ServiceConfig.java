package org.eos.aniki.bot.services;

import java.util.Random;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global configuration for the services.
 *
 * @author Eos
 */
@Configuration
public class ServiceConfig {

    /**
     * A random generator to fetch random posts.
     *
     * @return A random generator.
     */
    @Bean
    public RandomGenerator random() {
        return new Random();
    }
}
