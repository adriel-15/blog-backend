package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDaoJpaIntegrationTest extends BaseJpaIntegrationTest{

    @Test
    @DisplayName("getByUsername(String username) - return user if exists.")
    void getByUsername_returnUser(){
        seeDefaultUser();

        Optional<User> userOptional = userDao.getByUsername("johnDoe");

        assertTrue(userOptional.isPresent());
        assertTrue(userOptional.get().isEnabled());
        assertNotNull(userOptional.get().getCreatedAt());

        assertEquals("johnDoe", userOptional.get().getUsername());
        assertEquals("test123", userOptional.get().getPassword());
        assertEquals("johnDoe@gmail.com", userOptional.get().getEmail());
        assertEquals(1, userOptional.get().getAuthorities().size());
        assertEquals(Authorities.ADMIN, userOptional.get().getAuthorities().iterator().next().getAuthority());
        assertEquals(Providers.BASIC, userOptional.get().getProvider().getProvider());
        assertEquals("john-doe-123", userOptional.get().getProfile().getProfileName());
        assertEquals(LocalDate.of(2000, Month.SEPTEMBER,15),userOptional.get().getProfile().getBirthDate());
    }

    @Test
    @DisplayName("getByUsername(String username) - return empty if user does not exists.")
    void getByUsername_returnEmpty(){
        Optional<User> result = userDao.getByUsername("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByProviderUID(String providerUID) - return user if exists.")
    void getByProviderUID_returnUser(){
        seeDefaultUser();

        Optional<User> result = userDao.getByProviderUID("1234");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("getByProviderUID(String providerUID) - return empty if user does not exists.")
    void getByProviderUID_returnEmpty(){
        Optional<User> result = userDao.getByProviderUID("1234");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("existsByEmail(boolean val) - return true if a user already have that email.")
    void existsByEmail_returnTrue(){
        seeDefaultUser();
        assertTrue(userDao.existsByEmail("johnDoe@gmail.com"));
    }

    @Test
    @DisplayName("existsByEmail(boolean val) - return false if no user have that email.")
    void existsByEmail_returnFalse(){
        assertFalse(userDao.existsByEmail("unknown@gmail.com"));
    }

    @Test
    @DisplayName("existsByProviderUID(String val) - return true if a user already have that ProviderUID.")
    void existsByProviderUID_returnTrue(){
        seeDefaultUser();
        assertTrue(userDao.existsByProviderUID("1234"));
    }

    @Test
    @DisplayName("existsByProviderUID(String val) - return false if no user have that ProviderUID.")
    void existsByProviderUID_returnFalse(){
        assertFalse(userDao.existsByProviderUID("unknow"));
    }


    @Test
    @DisplayName("existsByUsername(String val) - return true if a user already have that username.")
    void existsByUsername_returnTrue(){
        seeDefaultUser();
        assertTrue(userDao.existsByUsername("johnDoe"));
    }

    @Test
    @DisplayName("existsByUsername(String val) - return false if no user have that username.")
    void existsByUsername_returnFalse(){
        assertFalse(userDao.existsByUsername("unknow"));
    }
}
