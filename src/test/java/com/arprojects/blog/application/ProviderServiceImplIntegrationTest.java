package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.AuthorityDto;
import com.arprojects.blog.domain.dtos.ProviderDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.ports.inbound.service_contracts.ProviderService;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class ProviderServiceImplIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @MockitoSpyBean
    private ProviderDao providerDao;

    @Autowired
    private ProviderService providerService;

    @BeforeEach
    void setUp(){
        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        entityManager.persist(provider);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void getByType_shouldReturnProviderDto_ifProviderTypeExists() throws ProviderNotFoundException {
        Providers providerType = Providers.BASIC;

        //first call should hit the database
        ProviderDto first = providerService.getByType(providerType);

        //second call should hit the cache
        ProviderDto second = providerService.getByType(providerType);

        assertEquals(providerType,second.provider());

        verify(providerDao,times(1)).getProviderByType(providerType);
    }

    @Test
    @Transactional
    void getByType_shouldThrowProviderNotFoundException_ifProviderTypeDoesNotExists(){
         Providers providerType = Providers.GOOGLE;

         assertThrows(ProviderNotFoundException.class, () -> providerService.getByType(providerType));
    }

}
