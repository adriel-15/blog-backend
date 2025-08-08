package com.arprojects.blog.ports.outbound.repository_contracts;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;

import java.util.Optional;

public interface AuthorityDao {
    Optional<Authority> getAuthorityByType(Authorities authorityType);
}
