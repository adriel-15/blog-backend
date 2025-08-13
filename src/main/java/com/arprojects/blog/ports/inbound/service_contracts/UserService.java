package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.AddGoogleUserDto;
import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.dtos.UserDto;
import com.arprojects.blog.domain.exceptions.*;

public interface UserService {
    boolean emailExists(String email);

    boolean providerUIDExists(String providerUID);

    UserDto getByProviderUID(String providerUID) throws UserNotFoundException;

    UserDto addGoogleUser(AddGoogleUserDto addGoogleUserDto) throws AuthorityNotFoundException, ProviderNotFoundException;

    void add(SignUpDto signUpDto) throws EmailAlreadyExistsException, UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException;

    boolean usernameExists(String username);
}
