package com.savostov.git_manager.service;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println("Request URL: " + req.getRequestURL());
        System.out.println("Request URI: " + req.getRequestURI());
        System.out.println("Query String: " + req.getQueryString());

        chain.doFilter(request, response);
    }


}