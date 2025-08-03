package com.arprojects.blog.ports.outbound.service_contracts;

import com.arprojects.blog.domain.dtos.GoogleInfoDto;

import java.util.Optional;

public interface GoogleAuthService {
    Optional<GoogleInfoDto> authenticate(String googleAccessToken);
}
