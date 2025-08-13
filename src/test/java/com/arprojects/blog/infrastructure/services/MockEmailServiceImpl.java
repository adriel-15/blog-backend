package com.arprojects.blog.infrastructure.services;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.infrastructure.aop.services.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class MockEmailServiceImpl implements EmailService {
    @Override
    public void sendSignUpEmail(SignUpDto signUpDto) {

    }
}
