package com.arprojects.blog.ports.outbound.repository_contracts;

import com.arprojects.blog.domain.entities.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> getUserByUsername(String username);
}
