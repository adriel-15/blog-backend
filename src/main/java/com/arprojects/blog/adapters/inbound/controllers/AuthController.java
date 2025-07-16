package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final JwtService jwtService;

    @Autowired
    public AuthController(JwtService jwtService){
        this.jwtService = jwtService;
    }

    @GetMapping("/")
    public String home(){
        return "!!Welcome to the ar-blog API!!";
    }

    @PostMapping("/login")
    public JwtDto login(Authentication authentication){
        return this.jwtService.generateJwt(authentication);
    }

}
