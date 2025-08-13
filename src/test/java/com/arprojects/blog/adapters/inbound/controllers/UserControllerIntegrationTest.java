package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoSpyBean
    UserService userService;

    @MockitoSpyBean
    UserDao userDao;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp(){

        cacheManager.getCache("authorityByType").clear();
        cacheManager.getCache("providerByType").clear();
        cacheManager.getCache("usersByProviderUID").clear();
        cacheManager.getCache("providerUIDExists").clear();
        cacheManager.getCache("emailExists").clear();

        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        entityManager.persist(provider);

        entityManager.flush();
        entityManager.clear();

        //create and persist Profile
        Profile profile = new Profile();
        profile.setProfileName("adriel-rosario15");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));
        entityManager.persist(profile);

        //create and persist user
        User user = new User();
        user.setUsername("adriel15Rosario2000");
        user.setPassword(passwordEncoder.encode("test123"));
        user.setEmail("adrielTest@gmail.com");
        user.setEnabled(true);
        user.setProvider(provider);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void signUpEndpoint_shouldReturnSuccessfullyCreatedMessage_ifUserCreated() throws Exception {
        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adriel15rosario@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("successfully created"));

        verify(userService, times(1)).add(any(SignUpDto.class));
        verify(userDao, times(1)).create(any(User.class));
    }

    @Test
    @Transactional
    void signUpEndpoint_shouldThrowEmailAlreadyExistsException_ifEmailIsNotvalid() throws Exception {
        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adrielTest@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                        .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void signUpEndpoint_shouldThrowUsernameAlreadyExistsException_ifUsernameIsNotValid() throws Exception {
        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adriel15Rosario2000",
                "adrielTewew@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                        .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void signUpEndpoint_shouldThrowProviderNotFoundException_ifProviderDoesNotExists() throws Exception {
        User user = entityManager.createQuery("from User where username=:username", User.class)
                .setParameter("username","adriel15Rosario2000")
                .getSingleResult();

        entityManager.remove(user);

        Provider provider = entityManager.createQuery("from Provider where providerType=:providerType",Provider.class)
                .setParameter("providerType",Providers.BASIC)
                .getSingleResult();

        entityManager.remove(provider);

        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adriel15Rosario23232",
                "adrielTewew@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                        .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    void signUpEndpoint_shouldThrowAuthorityNotFoundException_ifAuthorityDoesNotExists() throws Exception{
        User user = entityManager.createQuery("from User where username=:username", User.class)
                .setParameter("username","adriel15Rosario2000")
                .getSingleResult();

        entityManager.remove(user);

        Authority authority = entityManager.createQuery("from Authority where authorityType=:authorityType",Authority.class)
                .setParameter("authorityType",Authorities.READER)
                .getSingleResult();

        entityManager.remove(authority);

        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adriel15Rosario23232",
                "adrielTewew@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    void signUpEndpoint_shouldThrowMethodArgumentNotValidException_ifSignUpDtoIsNotValid() throws Exception{
        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adriel15",
                "adrielTewew@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "alex2"
        );

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                        .andExpect(status().isBadRequest());
    }
}
