package com.arprojects.blog.domain.exceptions;

public class GoogleLoginFailedException extends Exception {

    public GoogleLoginFailedException(String message){
        super(message);
    }

}
