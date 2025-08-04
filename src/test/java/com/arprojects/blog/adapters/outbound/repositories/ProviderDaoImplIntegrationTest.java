package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
public class ProviderDaoImplIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProviderDao providerDao;

    @BeforeEach
    void setup(){
        //create and persist provider
        Provider provider = new Provider(Providers.BASIC);
        entityManager.persist(provider);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void getProviderByType_shouldReturnProvider_whenTypeExists(){
        Optional<Provider> result = providerDao.getProviderByType(Providers.BASIC);

        assertTrue(result.isPresent());
        assertEquals(Providers.BASIC.getLabel(),result.get().getProvider().getLabel());
    }

    @Test
    @Transactional
    void getProviderByType_shouldReturnEmpty_whenTypeExists(){
        Optional<Provider> result = providerDao.getProviderByType(Providers.GOOGLE);

        assertTrue(result.isEmpty());
    }
}
