package com.arprojects.blog.domain.entities;

import com.arprojects.blog.domain.enums.Authorities;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {


    @Test
    void test_userEntity_gettersAndSetters(){
        //arrange
        long expectedId = 1;
        String expectedUsername = "alex19";
        String expectedPassword = "test1234";
        String expectedEmail = "alex19rosario@gmail.com";
        String expectedProviderUID = "provider-unique-id";
        Set<Authority> expectedAuthorities = Set.of(
                new Authority(Authorities.ADMIN),
                new Authority(Authorities.WRITER),
                new Authority(Authorities.READER)
        );

        //act
        User user = new User();
        user.setId(expectedId);
        user.setEnabled(true);
        user.setUsername(expectedUsername);
        user.setPassword(expectedPassword);
        user.setEmail(expectedEmail);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setProviderUniqueId(expectedProviderUID);
        user.setProvider(new Provider());
        user.setAuthorities(expectedAuthorities);
        user.setProfile(new Profile());

        //assert
        assertEquals(expectedId,user.getId());
        assertTrue(user.isEnabled());
        assertEquals(expectedUsername,user.getUsername());
        assertEquals(expectedPassword,user.getPassword());
        assertEquals(expectedEmail,user.getEmail());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(expectedProviderUID,user.getProviderUniqueId());
        assertNotNull(user.getProvider());
        assertNotNull(user.getProfile());
        assertNotNull(user.getAuthorities());
        assertEquals(3,user.getAuthorities().size());

    }
}
