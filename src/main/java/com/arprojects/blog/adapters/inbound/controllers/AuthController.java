package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController {

    private final JwtService jwtService;

    @Autowired
    public AuthController(JwtService jwtService){
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public JwtDto login(Authentication authentication){
        return this.jwtService.generateJwt(authentication);
    }

    @PostMapping("/google")
    public JwtDto googleLogin(@RequestBody GoogleLoginDto googleLoginDto) throws
            GoogleLoginFailedException,
            UserNotFoundException,
            ProviderNotFoundException,
            AuthorityNotFoundException,
            EmailAlreadyExistsException
    {
        return this.jwtService.generateJwt(googleLoginDto);
    }

}
