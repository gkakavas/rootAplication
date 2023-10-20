package com.example.app.security;

import com.example.app.entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendRedirect( "/access-denied");
        log.error(request.getHeader("Authorization"));
        log.error(request.getMethod());
        log.error(request.getRequestURI());
        log.error(accessDeniedException.getMessage());
        log.error(String.valueOf(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()));
        log.error(request.getRemoteAddr());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.err.println(user);
        accessDeniedException.printStackTrace();
    }
}
