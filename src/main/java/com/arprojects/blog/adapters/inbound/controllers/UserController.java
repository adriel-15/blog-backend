package com.arprojects.blog.adapters.inbound.controllers;

import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.dtos.VerifyResetPasswordCodeDto;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public UserController(UserService userService,JwtService jwtService){

        this.userService = userService;
        this.jwtService = jwtService;
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

    @PostMapping("/get-reset-password-code")
    public void generateResetPasswordCode(@RequestBody String email) throws EmailNotFoundException {
        userService.generateResetPasswordCode(email);
    }

    @PostMapping("/verify-reset-password-code")
    public JwtDto verifyResetPasswordCode(@RequestBody VerifyResetPasswordCodeDto verifyResetPasswordCodeDto){
        return jwtService.generateJwt(verifyResetPasswordCodeDto);
    }

    @PostMapping("/update-password")
    public void updateUserPassword(@RequestBody String newPassword, Authentication authentication){
        userService.updatePassword(newPassword, authentication); //this service should only be allow to use if the jwt reset token is valid
        //and it should only allow to update the password of the in the token
    }


}
