package com.careconnect.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class WebRuntimeConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/provider/{path:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/provider/{path:[^\\.]*}/{id:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/admin/{path:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/portal/{path:[^\\.]*}").setViewName("forward:/index.html");
        registry.addViewController("/settings").setViewName("forward:/index.html");
    }

    @Configuration
    static class CacheHeadersFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            filterChain.doFilter(request, response);
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) response.setHeader("Cache-Control", "private, no-store");
            else if (path.matches(".*-[A-Z0-9]+\\.(js|css)$")) response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            else if (!path.contains(".")) response.setHeader("Cache-Control", "no-cache");
        }
    }
}
