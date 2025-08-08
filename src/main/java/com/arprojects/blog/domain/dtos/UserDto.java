package com.arprojects.blog.domain.dtos;

import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;

import java.util.Set;

public record UserDto(
        long id,
        String email,
        Providers provider,
        Set<Authorities> authorities,
        ProfileDto profile
) {
}
