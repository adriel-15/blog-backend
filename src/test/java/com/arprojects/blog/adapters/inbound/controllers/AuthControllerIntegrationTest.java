package com.arprojects.blog.adapters.inbound.controllers;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
        @ConfigureWireMock(port = 8888)
})
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    private RestTestClient client;

    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0")           // ← this is the new non-deprecated class
                    .asCompatibleSubstituteFor("mysql") // ← makes JDBC URL work with Spring Boot
    )
            .withDatabaseName("arblog_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);   // optional, but you use it

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AuthorityDao authorityDao;

    @Autowired
    private ProviderDao providerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private GoogleAuthService googleAuthService;

    @BeforeAll
    static void beforeAll(){
        mysql.start();
        mysql.withReuse(true);
    }

    @AfterAll
    static void afterAll(){
        mysql.stop();
    }

    @BeforeEach
    void setup(){
        client = RestTestClient.bindToServer().baseUrl("http://localhost:"+port).build();

        clearCaches();
        deleteAll();

        seedAuthorities();
        seedProviders();
    }

    @Test
    void basicLogin_shouldReturnJwt(){
        seedDefaultUser();

        JwtDto jwt = client.post()
                .uri("/login")
                .headers(h -> h.setBasicAuth("adriel15","test123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtDto.class)
                .returnResult()
                .getResponseBody();

        Jwt decodedJwt = jwtDecoder.decode(jwt.token());

        assertEquals("adriel15",decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_ADMIN"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));
        assertNotNull(decodedJwt.getClaimAsString("profileName"));
    }

    @Test
    void basicLogin_shouldReturnUnauthorized_ifBadCredentials(){
        client.post()
                .uri("/login")
                .headers(h -> h.setBasicAuth("invalid user","invalid password"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void googleLogin_shouldReturnJwt(){
        seedGoogleUser();

        stubFor(get("/userinfo").withHeader("Authorization",containing("Bearer"))
                .willReturn(okJson("""
                        {
                            "sub": "123",
                            "email": "test@example.com",
                            "name": "John Doe"
                        }
                        """)));

        JwtDto jwt = client.post()
                .uri("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GoogleLoginDto("valid-access-token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtDto.class)
                .returnResult()
                .getResponseBody();

        assert jwt != null;
        Jwt decodedJwt = jwtDecoder.decode(jwt.token());

        assertEquals("test@example.com", decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_READER"));
        assertEquals("John Doe", decodedJwt.getClaimAsString("profileName"));

    }

    @Test
    void googleLogin_throwGoogleLoginFailedException(){
        stubFor(get("/userinfo").willReturn(unauthorized()));

        client.post().uri("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void googleLogin_throwEmailAlreadyExistsException(){

        seedGoogleUser();

        stubFor(get("/userinfo").withHeader("Authorization",containing("Bearer"))
                .willReturn(okJson("""
                        {
                            "sub": "1234",
                            "email": "test@example.com",
                            "name": "John Doe"
                        }
                        """)));

        client.post()
                .uri("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GoogleLoginDto("valid-access-token"))
                .exchange()
                .expectStatus().is4xxClientError();

    }

    @Test
    void googleLogin_shouldReturnJwtAndCreateUser_ifUserIsNew(){
        seedGoogleUser();

        stubFor(get("/userinfo").withHeader("Authorization",containing("Bearer"))
                .willReturn(okJson("""
                        {
                            "sub": "1234",
                            "email": "test2@example.com",
                            "name": "John Doe 2"
                        }
                        """)));

        var jwt = client.post()
                .uri("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GoogleLoginDto("valid-access-token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtDto.class)
                .returnResult()
                .getResponseBody();

        assert jwt != null;
        Jwt decodedJwt = jwtDecoder.decode(jwt.token());

        assertEquals("test2@example.com", decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_READER"));
        assertEquals("John Doe 2", decodedJwt.getClaimAsString("profileName"));
    }

    // ------ helper methods -------
    private void clearCaches(){
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    private void seedAuthorities(){
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        authorityDao.create(authority);

        // Add these if they're needed for multiple tests
        Authority readerAuthority = new Authority();
        readerAuthority.setAuthority(Authorities.READER);
        authorityDao.create(readerAuthority);
    }

    private void seedProviders(){
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        providerDao.create(provider);

        Provider googleProvider = new Provider();
        googleProvider.setProvider(Providers.GOOGLE);
        providerDao.create(googleProvider);
    }

    private void seedDefaultUser(){
        Profile profile = new Profile();
        profile.setProfileName("adriel-rosario15");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));

        var provider = providerDao.getProviderByType(Providers.BASIC);
        var adminAuthority = authorityDao.getAuthorityByType(Authorities.ADMIN);

        //create and persist user
        User user = new User();
        user.setUsername("adriel15");
        user.setPassword(encoder.encode("test123"));
        user.setEmail("adrielTest@gmail.com");
        user.setEnabled(true);
        user.setProfile(profile);

        provider.ifPresent(user::setProvider);
        adminAuthority.ifPresent(authority -> user.setAuthorities(Set.of(authority)));

        userDao.create(user);
    }

    private void seedGoogleUser(){

        Profile profile = new Profile();
        profile.setProfileName("John Doe");

        var provider = providerDao.getProviderByType(Providers.GOOGLE);
        var readerAuthority = authorityDao.getAuthorityByType(Authorities.READER);

        User user = new User();
        user.setEmail("test@example.com");
        user.setProviderUniqueId("123");
        user.setEnabled(true);
        user.setProfile(profile);

        if(provider.isEmpty())
            System.out.println("Provider is NULL!!!");

        provider.ifPresent(user::setProvider);
        readerAuthority.ifPresent(auth -> user.setAuthorities(Set.of(auth)));


        userDao.create(user);
    }

    private void deleteAll(){
        userDao.deleteAll();
        providerDao.deleteAll();
        authorityDao.deleteAll();
    }
}