package com.github.vibek.nic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate; // <-- Import this

@Configuration
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- NEW BEAN START ---
    // This bean is used for making outbound HTTP requests,
    // which we will need to call the Expo Push API.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    // --- NEW BEAN END ---
}