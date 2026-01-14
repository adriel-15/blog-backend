package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AuthorityDaoJpaIntegrationTest extends BaseJpaIntegrationTest {

    @Test
    @DisplayName("getAuthorityByType(Enum Authorities) - should return Authority if exists.")
    void getAuthorityByTypeShouldReturn(){
        seedAuthority(); //creates new Authority

        Optional<Authority> authority = authorityDao.getByType(Authorities.ADMIN);

        assertTrue(authority.isPresent());
        assertEquals(Authorities.ADMIN,authority.get().getAuthority());
    }

    @Test
    @DisplayName("getAuthorityByType(Enum Authorities) - should return empty Optional if Authority does not exists.")
    void getByTypeShouldReturnEmpty(){
        Optional<Authority> authority = authorityDao.getByType(Authorities.ADMIN);

        assertTrue(authority.isEmpty());
    }

    @Test
    @DisplayName("create(Authority authority) - should create a new Authority.")
    void shouldSaveNewAuthority(){
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);

        authorityDao.save(authority);

        Optional<Authority> newAuthority = authorityDao.getByType(Authorities.READER);

        assertTrue(newAuthority.isPresent());
        assertEquals(Authorities.READER,newAuthority.get().getAuthority());
    }
    @Test
    @DisplayName("deleteAll() - should delete all the authorities in the DB.")
    void deleteAllAuthorities(){
        authorityDao.deleteAll();

        Optional<Authority> authority = authorityDao.getByType(Authorities.ADMIN);

        assertTrue(authority.isEmpty());
    }


}
