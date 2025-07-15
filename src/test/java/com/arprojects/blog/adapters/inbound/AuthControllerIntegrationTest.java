package com.arprojects.blog.adapters.inbound;

import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp(){
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        entityManager.persist(authority);

        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        entityManager.persist(provider);

        //create and persist Profile
        Profile profile = new Profile();
        profile.setProfileName("adriel-rosario15");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));
        entityManager.persist(profile);

        //create and persist user
        User user = new User();
        user.setUsername("adriel15");
        user.setPassword(encoder.encode("test123"));
        user.setEmail("adrielTest@gmail.com");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setProvider(provider);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    public void loginEndpoint_returnJwt_ifCredentialsAreValid() throws Exception{
        MvcResult result =  mockMvc.perform(post("/login")
                .with(httpBasic("adriel15","test123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").hasJsonPath())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        JwtDto jwtDto = objectMapper.readValue(responseJson, JwtDto.class);

        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals(decodedJwt.getSubject(),"adriel15");
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_ADMIN"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));

    }

    @Test
    @Transactional
    public void loginEndpoint_returnUnauthorizedStatus_ifCredentialsAreNotValid() throws Exception{
        mockMvc.perform(post("/login")
                .with(httpBasic("bad","credentials")))
                .andExpect(status().isUnauthorized());
    }

}
