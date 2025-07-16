package com.arprojects.blog.domain.entities;

import com.arprojects.blog.domain.enums.Authorities;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class AuthorityTest {

    @Test
    void test_authorityEntity_GettersAndSetters() {
        //arrange
        Authority authority = new Authority();

        //act
        authority.setId(1);
        authority.setAuthority(Authorities.ADMIN);
        authority.setUsers(List.of(new User()));

        //assert
        assertEquals(1,authority.getId());
        assertEquals(Authorities.ADMIN,authority.getAuthority());
        assertNotNull(authority.getUsers());
        assertEquals(1,authority.getUsers().size());
    }


}
