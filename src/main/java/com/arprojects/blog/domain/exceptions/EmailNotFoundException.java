package com.arprojects.blog.domain.exceptions;

public class EmailNotFoundException extends Exception {

    public EmailNotFoundException(String message){
        super(message);
    }
}
