package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
        @ConfigureWireMock(port = 8888)
})
@ActiveProfiles("test")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    private RestTestClient client;

    @LocalServerPort
    private int port;

    @Autowired
    private GoogleAuthService googleAuthService;

    @BeforeAll
    static void beforeAll(){
        mysql.start();
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
    @DisplayName("POST /login - should return a jwt if user credentials are valid.")
    void basicLogin_shouldReturnJwt(){
        seedDefaultUser();

        JwtDto jwt = client.post()
                .uri("/login")
                .headers(h -> h.setBasicAuth("adriel15Rosario123","test123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtDto.class)
                .returnResult()
                .getResponseBody();

        assert jwt != null;
        Jwt decodedJwt = jwtDecoder.decode(jwt.token());

        assertEquals("adriel15Rosario123",decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_ADMIN"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));
        assertNotNull(decodedJwt.getClaimAsString("profileName"));
    }

    @Test
    @DisplayName("POST /login - should return http status unauthorized if user credentials are invalid.")
    void basicLogin_shouldReturnUnauthorized_ifBadCredentials(){
        client.post()
                .uri("/login")
                .headers(h -> h.setBasicAuth("invalid user","invalid password"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /google - should return a jwt if user credentials are valid.")
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
    @DisplayName("POST /google - should throw a GoogleLoginFailedException if user credentials are invalid")
    void googleLogin_throwGoogleLoginFailedException(){
        stubFor(get("/userinfo").willReturn(unauthorized()));

        client.post().uri("/google")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /google - should throw a EmailAlreadyExistsException if user email already exists.")
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
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

    }

    @Test
    @DisplayName("POST /google - should return a jwt if user credentials are valid and create new user if no exist.")
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

}