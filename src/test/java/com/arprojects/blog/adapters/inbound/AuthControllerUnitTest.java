package com.arprojects.blog.adapters.inbound;

import com.arprojects.blog.adapters.inbound.controllers.AuthController;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void loginEndpoint_shouldReturnJwt() throws Exception{
        JwtDto jwtDto = new JwtDto("test-token");

        when(jwtService.generateJwt(any(Authentication.class))).thenReturn(jwtDto);

        mockMvc.perform(post("/login").with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtDto.token()));
    }
}
