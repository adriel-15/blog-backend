package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ProviderDaoJpaIntegrationTest extends BaseJpaIntegrationTest {

    @Test
    @DisplayName("getProviderByType(Enum Providers) - should return Provider if exists.")
    void getProviderByTypeReturn(){
        seedProvider(); //create new Provider

        Optional<Provider> provider = providerDao.getByType(Providers.BASIC);

        assertTrue(provider.isPresent());
        assertEquals(Providers.BASIC,provider.get().getProvider());
    }

}
