package com.arprojects.blog.domain.exceptions;

public class AuthorityNotFoundException extends Exception{

    public AuthorityNotFoundException(String message){
        super(message);
    }

}
