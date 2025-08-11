package com.github.vibek.nic.config;
//package com.github.vibek.nic.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//
//@Configuration
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/auth/login", "/auth/logout").permitAll() // Allow login/logout without authentication
//                .anyRequest().authenticated() // All other endpoints require authentication
//            )
//                .formLogin(form -> form
//                        .loginPage("/auth/login")
//                        .usernameParameter("email"))
//            .httpBasic(Customizer.withDefaults()); // Use HTTP Basic Auth for demonstration (replace with JWT for production)
//        return http.build();
//    }
//}
