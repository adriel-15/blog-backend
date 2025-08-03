package com.arprojects.blog.ports.outbound.repository_contracts;

import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.exceptions.*;

import java.util.Optional;

public interface UserDao {
    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByProviderUID(String providerUID);

    void create(User user) throws EmailAlreadyExistsException, UsernameAlreadyExistsException, ProviderUIDAlreadyExistsException, DuplicateKeyException, UserCreationException;
}
