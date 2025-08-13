package com.arprojects.blog.infrastructure.aspects;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.domain.exceptions.EmailAlreadyExistsException;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.domain.exceptions.UsernameAlreadyExistsException;
import com.arprojects.blog.infrastructure.aop.services.EmailService;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class EmailAspectIntegrationTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp(){
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
    }

    @Test
    @Transactional
    void addWithSignUpDto_shouldTrigger_sendSignUpEmail() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException {
        SignUpDto signUpDto = new SignUpDto(
                "alex19",
                "alex19rosario@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        userService.add(signUpDto);

        verify(emailService,times(1)).sendSignUpEmail(signUpDto);
    }
}
