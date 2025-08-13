package com.arprojects.blog.infrastructure.aop.services;

import com.arprojects.blog.domain.dtos.SignUpDto;

public interface EmailService {
    void sendSignUpEmail(SignUpDto signUpDto);
}
