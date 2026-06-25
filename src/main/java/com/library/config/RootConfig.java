package com.library.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.library.dao",
        "com.library.service",
        "com.library.security"
})
public class RootConfig {
}