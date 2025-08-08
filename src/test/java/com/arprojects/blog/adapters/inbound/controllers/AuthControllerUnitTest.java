package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.adapters.inbound.AbstractControllerUnitTest;
import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(AuthController.class)
class AuthControllerUnitTest extends AbstractControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;
    @Test
    void homeEndpoint_shouldReturnApiRoutes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("!!Welcome to the ar-blog API!!")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/google")));
    }

    @Test
    @WithMockUser(username = "test-user")
    void basicLoginEndpoint_shouldReturnJwt() throws Exception {
        JwtDto jwtDto = new JwtDto("test-token");

        when(jwtService.generateJwt(any(Authentication.class))).thenReturn(jwtDto);

        mockMvc.perform(post("/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void googleLoginEndpoint_shouldReturnJwtDto_ifAccessTokenIsValid() throws Exception {
        JwtDto jwtDto = new JwtDto("test-token");
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("google-access-token");

        when(jwtService.generateJwt(any(GoogleLoginDto.class))).thenReturn(jwtDto);

        mockMvc.perform(post("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleLoginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtDto.token()));
    }

    @Test
    void googleLoginEndpoint_shouldThrowGoogleLoginFailedException_ifGoogleServiceFails() throws Exception {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-access-token");

        when(jwtService.generateJwt(any(GoogleLoginDto.class))).thenThrow(new GoogleLoginFailedException("google login fail"));

        mockMvc.perform(post("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleLoginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void googleLoginEndpoint_shouldThrowUserNotFoundException_ifGetUserByProviderUIDFails() throws Exception{
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-access-token");

        when(jwtService.generateJwt(any(GoogleLoginDto.class))).thenThrow(new UserNotFoundException("not found"));

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginDto)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void googleLoginEndpoint_shouldThrowProviderNotFoundException_ifProviderDoesNotExists() throws Exception{
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-access-token");

        when(jwtService.generateJwt(any(GoogleLoginDto.class))).thenThrow(new ProviderNotFoundException("not found"));

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void googleLoginEndpoint_shouldThrowAuthorityNotFoundException_ifAuthorityDoesNotExists() throws Exception{
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-access-token");

        when(jwtService.generateJwt(any(GoogleLoginDto.class))).thenThrow(new AuthorityNotFoundException("not found"));

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void googleLoginEndpoint_shouldThrowEmailAlreadyExistsException_ifEmailIsDuplicate() throws Exception{
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-access-token");

        when(jwtService.generateJwt(any(GoogleLoginDto.class))).thenThrow(new EmailAlreadyExistsException("duplicate email"));

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginDto)))
                .andExpect(status().isConflict());
    }

}