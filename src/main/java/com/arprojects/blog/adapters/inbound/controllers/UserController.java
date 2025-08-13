package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.domain.exceptions.EmailAlreadyExistsException;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.domain.exceptions.UsernameAlreadyExistsException;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public Map<String, String> signUp(@Valid @RequestBody SignUpDto signUpDto) throws
            EmailAlreadyExistsException,
            UsernameAlreadyExistsException,
            ProviderNotFoundException,
            AuthorityNotFoundException
    {

        userService.add(signUpDto);

        return Map.of("message","successfully created");
    }
}
