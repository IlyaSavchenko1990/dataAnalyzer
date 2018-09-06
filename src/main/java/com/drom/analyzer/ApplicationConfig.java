package com.drom.analyzer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring application config class
 *
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
public class ApplicationConfig {

    @Bean
    public Main main() {
        return new Main();
    }
}
