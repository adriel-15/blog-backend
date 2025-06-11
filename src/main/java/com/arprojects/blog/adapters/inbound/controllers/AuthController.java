package com.arprojects.blog.adapters.inbound.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/")
    public String home(){
        return "!!Welcome to the ar-blog API!!";
    }
}
