package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.*;
import com.arprojects.blog.domain.exceptions.*;
import org.springframework.security.core.Authentication;

public interface UserService {
    boolean existsByEmail(String email);

    boolean existsByProviderUID(String providerUID);

    UserDto getByProviderUID(String providerUID) throws UserNotFoundException;

    UserDto addGoogleUser(AddGoogleUserDto addGoogleUserDto) throws AuthorityNotFoundException, ProviderNotFoundException;

    void add(SignUpDto signUpDto) throws EmailAlreadyExistsException, UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException;

    boolean existsByUsername(String username);

    String generateResetPasswordCode(String email) throws EmailNotFoundException;

    void updatePassword(String password, Authentication authentication);
}
