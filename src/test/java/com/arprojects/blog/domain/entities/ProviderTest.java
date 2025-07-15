package com.arprojects.blog.domain.entities;

import com.arprojects.blog.domain.enums.Providers;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProviderTest {

    @Test
    void test_providerEntity_GettersAndSetters(){
        //arrange
        Provider provider = new Provider();
        int expectedId = 1;
        Providers expectedProvider = Providers.BASIC;

        //act
        provider.setId(expectedId);
        provider.setProvider(expectedProvider);
        provider.setUsers(List.of(new User()));

        //assert
        assertEquals(expectedId,provider.getId());
        assertEquals(expectedProvider,provider.getProvider());
        assertNotNull(provider.getUsers());
        assertEquals(1,provider.getUsers().size());
    }
}
