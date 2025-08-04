package com.arprojects.blog.domain.exceptions;

public class AuthorityNotFoundException extends RuntimeException{

    public AuthorityNotFoundException(String message){
        super(message);
    }

}
