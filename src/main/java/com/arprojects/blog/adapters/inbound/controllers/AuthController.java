package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.stream.Collectors;

@RestController
public class AuthController {

    private final JwtService jwtService;
    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public AuthController(JwtService jwtService, RequestMappingHandlerMapping handlerMapping){
        this.jwtService = jwtService;
        this.handlerMapping =handlerMapping;
    }

    @GetMapping("/")
    public String home(){
        String routes = handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> entry.getKey().getPatternValues().stream()
                        .noneMatch(pattern -> pattern.contains("/error"))) // 👈 filter out error paths
                .map(entry -> {
                    var methods = entry.getKey().getMethodsCondition().getMethods();
                    var patterns = entry.getKey().getPatternValues();
                    return methods + " " + patterns;
                })
                .collect(Collectors.joining("<br>"));
        return "<h2>!!Welcome to the ar-blog API!!</h2><h4>Available routes:</h4>" + routes;
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
