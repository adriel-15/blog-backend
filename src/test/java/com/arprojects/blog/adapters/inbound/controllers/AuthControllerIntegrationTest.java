package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.GoogleInfoDto;
import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
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

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private GoogleAuthService googleAuthService;

    @MockitoSpyBean
    private UserDao userDao;

    @MockitoSpyBean
    private AuthorityDao authorityDao;

    @MockitoSpyBean
    private ProviderDao providerDao;

    @BeforeEach
    void setUp(){
        cacheManager.getCache("authorityByType").clear();
        cacheManager.getCache("providerByType").clear();
        cacheManager.getCache("usersByProviderUID").clear();
        cacheManager.getCache("providerUIDExists").clear();
        cacheManager.getCache("emailExists").clear();

        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        entityManager.persist(authority);

        // Add these if they're needed for multiple tests
        Authority readerAuthority = new Authority();
        readerAuthority.setAuthority(Authorities.READER);
        entityManager.persist(readerAuthority);

        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        entityManager.persist(provider);

        Provider googleProvider = new Provider();
        googleProvider.setProvider(Providers.GOOGLE);
        entityManager.persist(googleProvider);

        entityManager.flush();

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
        user.setProvider(provider);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void homeEndpoint_returnApiRoutes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("!!Welcome to the ar-blog API!!")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/google")));
    }

    @Test
    @Transactional
    void basicLoginEndpoint_returnJwt_ifCredentialsAreValid() throws Exception{
        MvcResult result =  mockMvc.perform(post("/login")
                .with(httpBasic("adriel15","test123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").hasJsonPath())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        JwtDto jwtDto = objectMapper.readValue(responseJson, JwtDto.class);

        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals("adriel15",decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_ADMIN"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));
        assertNotNull(decodedJwt.getClaimAsString("profileName"));
    }

    @Test
    @Transactional
    void basicLoginEndpoint_returnUnauthorizedStatus_ifCredentialsAreNotValid() throws Exception{
        mockMvc.perform(post("/login")
                .with(httpBasic("bad","credentials")))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Transactional
    void googleLoginEndpoint_shouldReturnJwtDto_ifAccessTokenIsValid() throws Exception{
        // Mock GoogleAuthService response
        GoogleInfoDto googleInfo = new GoogleInfoDto("12345", "Test User","test@example.com");
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.of(googleInfo));

        Authority authority = entityManager.createQuery(
                "select a from Authority a where a.authorityType = :authorityType",
                Authority.class
        ).setParameter("authorityType",Authorities.READER).getSingleResult();

        Provider provider = entityManager.createQuery(
                "select p from Provider p where p.providerType = :providerType",
                Provider.class
        ).setParameter("providerType",Providers.GOOGLE).getSingleResult();

        Profile profile = new Profile();
        profile.setProfileName("Test User");
        entityManager.persist(profile);

        User user = new User();
        user.setEmail("test@example.com");
        user.setProviderUniqueId("12345");
        user.setEnabled(true);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        user.setProvider(provider);
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("valid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        MvcResult result = mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andReturn();


        // Verify the JWT content
        String responseJson = result.getResponse().getContentAsString();
        JwtDto jwtDto = objectMapper.readValue(responseJson, JwtDto.class);
        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals("test@example.com", decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_READER"));
        assertEquals("Test User", decodedJwt.getClaimAsString("profileName"));
    }

    @Test
    @Transactional
    void googleLoginEndpoint_shouldThrowGoogleLoginFailedException_ifGoogleServiceFails() throws Exception{
        // Mock GoogleAuthService response
        //GoogleInfoDto googleInfo = new GoogleInfoDto("12345", "Test User","test@example.com");
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.empty());

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void googleLoginEndpoint_shouldThrowUserNotFoundException_ifGetUserByProviderUIDFails() throws Exception{
        // Mock GoogleAuthService response
        GoogleInfoDto googleInfo = new GoogleInfoDto("12345", "Test User","test@example.com");
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.of(googleInfo));

        when(userDao.getUserByProviderUID(anyString())).thenReturn(Optional.empty());

        Authority authority = entityManager.createQuery(
                "select a from Authority a where a.authorityType = :authorityType",
                Authority.class
        ).setParameter("authorityType",Authorities.READER).getSingleResult();

        Provider provider = entityManager.createQuery(
                "select p from Provider p where p.providerType = :providerType",
                Provider.class
        ).setParameter("providerType",Providers.GOOGLE).getSingleResult();

        Profile profile = new Profile();
        profile.setProfileName("Test User");
        entityManager.persist(profile);

        User user = new User();
        user.setEmail("test@example.com");
        user.setProviderUniqueId("12345");
        user.setEnabled(true);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        user.setProvider(provider);
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("valid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    void googleLoginEndpoint_shouldThrowAuthorityNotFoundException_ifAuthorityDoesNotExists() throws Exception{
        // Mock GoogleAuthService response
        GoogleInfoDto googleInfo = new GoogleInfoDto("new-user-id","New User","new@example.com" );
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.of(googleInfo));

        when(authorityDao.getAuthorityByType(any(Authorities.class))).thenReturn(Optional.empty());

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("valid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void googleLoginEndpoint_shouldThrowProviderNotFoundException_ifProviderDoesNotExists() throws Exception{
        // Mock GoogleAuthService response
        GoogleInfoDto googleInfo = new GoogleInfoDto("new-user-id","New User","new@example.com" );
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.of(googleInfo));

        when(providerDao.getProviderByType(any(Providers.class))).thenReturn(Optional.empty());

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("valid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void googleLoginEndpoint_shouldThrowEmailAlreadyExistsException_ifEmailIsDuplicate() throws Exception{
        // Mock GoogleAuthService response
        GoogleInfoDto googleInfo = new GoogleInfoDto("new-user-id","New User","new@example.com" );
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.of(googleInfo));

        when(userDao.emailExists(anyString())).thenReturn(true);

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("valid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        mockMvc.perform(post("/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void googleLoginEndpoint_returnJwtDto_ifCredentialsAreValidAndUserDoesNotExist() throws Exception{
        // Mock GoogleAuthService response
        GoogleInfoDto googleInfo = new GoogleInfoDto("new-user-id","New User","new@example.com" );
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.of(googleInfo));

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("valid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        MvcResult result = mockMvc.perform(post("/google")
                        .contentType("application/json")
                        .content(requestBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andReturn();

        // Verify the JWT content
        String responseJson = result.getResponse().getContentAsString();
        JwtDto jwtDto = objectMapper.readValue(responseJson, JwtDto.class);
        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals("new@example.com", decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_READER"));
        assertEquals("New User", decodedJwt.getClaimAsString("profileName"));

        // Verify user was created in database
        User createdUser = entityManager.createQuery("SELECT u FROM User u WHERE u.providerUniqueId = :providerId", User.class)
                .setParameter("providerId", "new-user-id")
                .getSingleResult();

        assertNotNull(createdUser);
        assertEquals("new@example.com", createdUser.getEmail());
    }

    @Test
    @Transactional
    void googleLoginEndpoint_returnUnauthorized_ifGoogleTokenIsInvalid() throws Exception {
        // Mock GoogleAuthService to return empty
        when(googleAuthService.authenticate(anyString()))
                .thenReturn(Optional.empty());

        // Perform the request
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-google-token");
        String requestBody = objectMapper.writeValueAsString(googleLoginDto);

        mockMvc.perform(post("/google")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

}
