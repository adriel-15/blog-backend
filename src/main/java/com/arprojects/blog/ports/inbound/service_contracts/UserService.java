package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.AddGoogleUserDto;
import com.arprojects.blog.domain.dtos.UserDto;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.domain.exceptions.UserNotFoundException;

public interface UserService {
    boolean emailExists(String email);

    boolean providerUIDExists(String providerUID);

    UserDto getUserByProviderUID(String providerUID) throws UserNotFoundException;

    UserDto addGoogleUser(AddGoogleUserDto addGoogleUserDto) throws AuthorityNotFoundException, ProviderNotFoundException;
}
