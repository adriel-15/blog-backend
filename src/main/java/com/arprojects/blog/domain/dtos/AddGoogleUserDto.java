package com.arprojects.blog.domain.dtos;

public record AddGoogleUserDto(String email,String providerUID,boolean enabled,String profileName) {
}
