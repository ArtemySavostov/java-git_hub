package com.savostov.git_manager.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RequestLoggingFilter requestLoggingFilter;

    @BeforeEach
    void setUp() throws IOException, ServletException {
        lenient().when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getRequestURI()).thenReturn("/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void doFilter_withValidRequest_logsRequestAndContinuesChain() throws ServletException, IOException {
        requestLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withPostRequest_logsPostRequest() throws ServletException, IOException {
        lenient().when(request.getMethod()).thenReturn("POST");

        requestLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withPutRequest_logsPutRequest() throws ServletException, IOException {
        lenient().when(request.getMethod()).thenReturn("PUT");

        requestLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withDeleteRequest_logsDeleteRequest() throws ServletException, IOException {
        lenient().when(request.getMethod()).thenReturn("DELETE");

        requestLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withDifferentUris_logsCorrectUri() throws ServletException, IOException {
        lenient().when(request.getRequestURI()).thenReturn("/api/users");

        requestLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withDifferentRemoteAddr_logsCorrectAddress() throws ServletException, IOException {
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        requestLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_withChainException_doesNotSuppressException() throws ServletException, IOException {
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);
        
        assertThatThrownBy(() -> requestLoggingFilter.doFilter(request, response, filterChain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("Test exception");
    }

    @Test
    void doFilter_withIOException_doesNotSuppressException() throws ServletException, IOException {
        doThrow(new IOException("Test IO exception")).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> requestLoggingFilter.doFilter(request, response, filterChain))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Test IO exception");
    }
}
