package com.arprojects.blog.domain.exceptions;

public class ProviderNotFoundException extends RuntimeException{

    public ProviderNotFoundException(String message){
        super(message);
    }
}
