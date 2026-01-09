package com.arprojects.blog.ports.outbound.repository_contracts;

import com.arprojects.blog.domain.dtos.UpdateUserPasswordDto;
import com.arprojects.blog.domain.entities.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> getByUsername(String username);

    Optional<User> getByProviderUID(String providerUID);

    void save(User user);

    boolean existsByEmail(String email);

    boolean existsByProviderUID(String providerUID);

    boolean existsByUsername(String username);

    void updatePasswordByEmail(UpdateUserPasswordDto updateUserPasswordDto);

    void deleteAll();
}
