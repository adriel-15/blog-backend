package com.arprojects.blog.adapters.outbound.services;

import com.arprojects.blog.domain.dtos.GoogleInfoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceImplUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleAuthServiceImpl googleAuthService;

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @Test
    void should_returnUser_when_allFieldsArePresent(){
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("123","John Doe","test@example.com");
        ResponseEntity<GoogleInfoDto> response = new ResponseEntity<>(googleInfoDto, HttpStatus.OK);

        when(restTemplate.exchange(
                ArgumentMatchers.eq(GOOGLE_USERINFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(GoogleInfoDto.class)
        )).thenReturn(response);

        Optional<GoogleInfoDto> result = googleAuthService.authenticate("valid-token");

        assertTrue(result.isPresent());
        assertEquals("123", result.get().sub());
        assertEquals("test@example.com", result.get().email());
        assertEquals("John Doe", result.get().name());
    }

    @Test
    void should_returnEmpty_when_nameFieldIsMissing() {
        GoogleInfoDto dto = new GoogleInfoDto("123", null, "Test User"); // missing email
        ResponseEntity<GoogleInfoDto> response = new ResponseEntity<>(dto, HttpStatus.OK);

        when(restTemplate.exchange(
                ArgumentMatchers.eq(GOOGLE_USERINFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(GoogleInfoDto.class)
        )).thenReturn(response);

        Optional<GoogleInfoDto> result = googleAuthService.authenticate("some-token");

        assertTrue(result.isEmpty());
    }


    @Test
    void should_returnEmpty_when_subFieldIsMissing() {
        GoogleInfoDto dto = new GoogleInfoDto(null, "adriel", "Test User"); // missing email
        ResponseEntity<GoogleInfoDto> response = new ResponseEntity<>(dto, HttpStatus.OK);

        when(restTemplate.exchange(
                ArgumentMatchers.eq(GOOGLE_USERINFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(GoogleInfoDto.class)
        )).thenReturn(response);

        Optional<GoogleInfoDto> result = googleAuthService.authenticate("some-token");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_returnEmpty_when_emailFieldIsMissing() {
        GoogleInfoDto dto = new GoogleInfoDto("12312", "adriel", null); // missing email
        ResponseEntity<GoogleInfoDto> response = new ResponseEntity<>(dto, HttpStatus.OK);

        when(restTemplate.exchange(
                ArgumentMatchers.eq(GOOGLE_USERINFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(GoogleInfoDto.class)
        )).thenReturn(response);

        Optional<GoogleInfoDto> result = googleAuthService.authenticate("some-token");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_returnEmpty_WhenRestTemplateThrowsException() {
        when(restTemplate.exchange(
                ArgumentMatchers.eq(GOOGLE_USERINFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(GoogleInfoDto.class)
        )).thenThrow(new RuntimeException("Network error"));

        Optional<GoogleInfoDto> result = googleAuthService.authenticate("some-token");

        assertTrue(result.isEmpty());
    }
}
