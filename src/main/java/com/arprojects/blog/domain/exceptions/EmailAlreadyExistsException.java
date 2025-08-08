package com.arprojects.blog.domain.exceptions;

public class EmailAlreadyExistsException extends Exception{

    public EmailAlreadyExistsException(String message){
        super(message);
    }

}
