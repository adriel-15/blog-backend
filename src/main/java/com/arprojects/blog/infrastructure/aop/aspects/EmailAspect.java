package com.arprojects.blog.infrastructure.aop.aspects;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.infrastructure.aop.services.EmailService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile({"prod","test"})
public class EmailAspect {

    private static final Logger log = LoggerFactory.getLogger(EmailAspect.class);
    private final EmailService emailService;

    @Autowired
    public EmailAspect(EmailService emailService){
        this.emailService = emailService;
    }

    @Pointcut("execution(public void com.arprojects.blog.ports.inbound.service_contracts.UserService.add(..))")
    private void forSignUpMethod(){}

    @AfterReturning("forSignUpMethod()")
    public void afterReturningSignUpAdvice(JoinPoint joinPoint){
        log.info("📩 EmailAspect triggered after add() method.");
        SignUpDto signUpDto = (SignUpDto) joinPoint.getArgs()[0];
        emailService.sendSignUpEmail(signUpDto);
    }
}
