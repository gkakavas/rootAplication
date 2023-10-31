package com.example.app.security;

import com.example.app.config.AllowedIpAddresses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IpAddressFilter extends OncePerRequestFilter {

    private final AllowedIpAddresses allowedIps;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
    final String requestIp = request.getRemoteAddr();
    final String requestEndPoint = request.getRequestURI();
    final String protectedUri = "/auth/register";
    if(!requestEndPoint.equals(protectedUri)){
        filterChain.doFilter(request,response);
        return;
    }
    if(allowedIps.getAllowedIps().contains(requestIp)){
        filterChain.doFilter(request,response);
    }
    else{
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
    }
}
