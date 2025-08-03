package com.arprojects.blog.domain.exceptions;

public class DuplicateKeyException extends RuntimeException{

    public DuplicateKeyException(String message){
        super(message);
    }
}
