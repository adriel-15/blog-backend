package com.arprojects.blog.domain.exceptions;

public class ProviderUIDAlreadyExistsException extends RuntimeException {
    public ProviderUIDAlreadyExistsException(String message){
        super(message);
    }
}
