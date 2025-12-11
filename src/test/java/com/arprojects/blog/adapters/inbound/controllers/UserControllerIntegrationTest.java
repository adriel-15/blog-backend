package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.dtos.SignUpResponseDto;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
        @ConfigureWireMock(port = 8888)
})
@ActiveProfiles("test")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    private RestTestClient client;

    @LocalServerPort
    private int port;

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
    @DisplayName("POST /signup - should return successfully created message.")
    void shouldReturnSuccessfullyCreated(){
        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "johnDoe001",
                "johnDoe001@gmail.com",
                "John Doe Jr",
                LocalDate.of(2000,9,15),
                "@VerySecurePassword"
        );

        SignUpResponseDto response = client.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(signUpDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SignUpResponseDto.class)
                .returnResult()
                .getResponseBody();

        assert response != null;
        assertEquals("Successfully created", response.message());
    }


    @Test
    @DisplayName("POST /signup - should throw EmailAlreadyExistsException if email already exists.")
    void shouldThrowEmailAlreadyExistsException(){
        seedDefaultUser(); //email -> adrielTest@gmail.com

        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "johnDoe001",
                "adrielTest@gmail.com",
                "John Doe Jr",
                LocalDate.of(2000,9,15),
                "@VerySecurePassword"
        );

        client.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(signUpDto)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    @DisplayName("POST /signup - should throw UsernameAlreadyExistsException if username already exists.")
    void shouldThrowUsernameAlreadyExistsException(){
        seedDefaultUser(); // username -> adriel15Rosario123

        //arrange
        SignUpDto signUpDto = new SignUpDto(
                "adriel15Rosario123",
                "johnDoe001@gmail.com",
                "John Doe Jr",
                LocalDate.of(2000,9,15),
                "@VerySecurePassword"
        );

        client.post()
                .uri("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(signUpDto)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

    }
}
