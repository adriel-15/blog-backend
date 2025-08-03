package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.exceptions.GoogleLoginFailedException;
import org.springframework.security.core.Authentication;

public interface JwtService {
    JwtDto generateJwt(Authentication authentication);

    JwtDto generateJwt(GoogleLoginDto googleLoginDto) throws GoogleLoginFailedException;
}
