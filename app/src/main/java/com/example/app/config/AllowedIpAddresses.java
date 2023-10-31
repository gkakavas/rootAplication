package com.example.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix ="allowed-ips")
public class AllowedIpAddresses {
    private List<String> ips;

    public List<String> getAllowedIps() {
        return ips;
    }
    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
