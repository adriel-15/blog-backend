package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.JwtDto;
import org.springframework.security.core.Authentication;

public interface JwtService {
    JwtDto generateJwt(Authentication authentication);
}
