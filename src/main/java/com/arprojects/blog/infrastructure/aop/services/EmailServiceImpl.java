package com.arprojects.blog.infrastructure.aop.services;

import com.arprojects.blog.domain.dtos.SignUpDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendSignUpEmail(SignUpDto signUpDto) {

        String body = "<h4>Congratulations!!</h4>"
                + "<p><strong>" + signUpDto.profileName() +
                "</strong>, your account has been successfully created.</p>";

        //send email logic
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adriel15rosario@gmail.com");
        message.setTo(signUpDto.email());
        message.setSubject("\uD83C\uDF89 Welcome to ARBLOG! Your account is ready");
        message.setText(body);
        javaMailSender.send(message);
    }
}
