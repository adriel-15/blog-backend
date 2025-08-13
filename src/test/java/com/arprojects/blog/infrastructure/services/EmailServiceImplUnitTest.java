package com.arprojects.blog.infrastructure.services;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.infrastructure.aop.services.EmailService;
import com.arprojects.blog.infrastructure.aop.services.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplUnitTest {

    private JavaMailSender javaMailSender;
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        emailService = new EmailServiceImpl(javaMailSender);
    }

    @Test
    void sendSignUpEmail_shouldCreateAndSendMessage() {
        // Arrange
        SignUpDto dto = new SignUpDto(
                "alex19",
                "alex19rosario@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );
        // Act
        emailService.sendSignUpEmail(dto);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("adriel15rosario@gmail.com", sentMessage.getFrom());
        assertEquals("alex19rosario@gmail.com", sentMessage.getTo()[0]);
    }
}
