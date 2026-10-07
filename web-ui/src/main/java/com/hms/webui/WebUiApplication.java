package com.hms.webui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;

/**
 * hms-common (a shared dependency) pulls in spring-boot-starter-security for the other
 * services' JWT validation filters. web-ui doesn't use Spring Security at all — auth is a
 * plain HTTP session plus a bearer token forwarded to the gateway (see security package) — so
 * its autoconfiguration is excluded here to stop it from installing a default login/CSRF chain
 * that would fight with the app's own /login flow.
 */
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
public class WebUiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebUiApplication.class, args);
    }
}
