package com.library.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Root Spring context.
 * Spring Core creates DAO and Service beans automatically using annotations:
 * @Repository for DAO classes and @Service for service classes.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.library.dao",
        "com.library.service"
})
public class RootConfig {
}