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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.HttpMethod.*;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    @Bean
    AccessDeniedHandler accessDeniedHandler() {
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

                        .requestMatchers(POST,"/event/create").hasAuthority("event::create")
                        .requestMatchers(POST,"/createGroupEvent/{id}").hasAuthority("event::createByGroup")
                        .requestMatchers(GET,"/event/").hasAuthority("event::readOne")
                        .requestMatchers(GET,"/event/{id}").hasAuthority("event::readAll")
                        .requestMatchers(PUT,"/event/update/{id}").hasAuthority("event::update")
                        .requestMatchers(DELETE,"/event/delete/{id}").hasAuthority("event::delete")
                        .requestMatchers(PATCH,"/addUsers/{eventId}").hasAuthority("event::addUsersToEvent")
                        .requestMatchers(PATCH,"/removeUsers/{eventId}").hasAuthority("event::removeUsersFromEvent")
                        .requestMatchers(PATCH,"/patchEventDetails/{eventId}").hasAuthority("event::patchEventDetails")

                        .requestMatchers(POST,"/group/create").hasAuthority("group::create")
                        .requestMatchers(GET,"/group/{id}").hasAuthority("group::readOne")
                        .requestMatchers(GET,"/group/").hasAuthority("group::readAll")
                        .requestMatchers(PUT,"/group/update/{id}").hasAuthority("group::update")
                        .requestMatchers(DELETE,"/group/delete/{id}").hasAuthority("group::delete")

                        .requestMatchers(POST,"/file/upload").hasAuthority("file::upload")
                        .requestMatchers(GET,"/file/download/evaluation/{fileId}").hasAuthority("file::downloadEvaluation")
                        .requestMatchers(GET,"/file/download/timesheet/{fileId}").hasAuthority("file::downloadTimesheet")
                        .requestMatchers(GET,"/file/evaluation/").hasAuthority("file::readAllEvaluations")
                        .requestMatchers(GET,"/file/timesheet/").hasAuthority("file::readAllTimesheets")
                        .requestMatchers(DELETE,"/file/delete/{fileId}").hasAuthority("file::delete")
                        .requestMatchers(PATCH,"/file/approveEvaluation/{fileId}").hasAuthority("file::approveEvaluation")

                        .requestMatchers("/access-denied").permitAll()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(customizer -> customizer.accessDeniedHandler(accessDeniedHandler()));

        return httpSecurity.build();
    }



}
