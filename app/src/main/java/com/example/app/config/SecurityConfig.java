package com.example.app.config;

import com.example.app.security.CustomAccessDeniedHandler;
import com.example.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/auth/authenticate").permitAll()
                        .requestMatchers("/auth/register").permitAll()

                        .requestMatchers(POST,"/user/create").hasAuthority("user::create")
                        .requestMatchers(GET,"/user/").hasAuthority("user::readAll")
                        .requestMatchers(GET,"/user/{id}").hasAuthority("user::readOne")
                        .requestMatchers(PUT,"/user/update/{id}").hasAuthority("user::update")
                        .requestMatchers(DELETE,"/user/delete/{id}").hasAuthority("user::delete")
                        .requestMatchers(PATCH,"/user/patch/{id}").hasAuthority("user::patch")
                        .requestMatchers(GET,"/user/{id}/events").hasAuthority("user::readUserEvents")
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((accessDeniedHandler)->accessDeniedHandler());
        return httpSecurity.build();
    }



}
