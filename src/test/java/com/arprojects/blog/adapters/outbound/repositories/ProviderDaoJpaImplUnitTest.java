package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProviderDaoJpaImplUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Provider> typedQuery;

    @InjectMocks
    private ProviderDaoJpaImpl providerDao;

    @Test
    void getProviderByType_shouldReturnProvider_whenTypeExists(){
        //arrange
        Provider provider = new Provider(Providers.BASIC);
        provider.setId(1);

        String query = "from Provider where providerType=:providerType";

        when(entityManager.createQuery(query,Provider.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("providerType",Providers.BASIC)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(provider);

        //act
        Optional<Provider> result = providerDao.getProviderByType(Providers.BASIC);

        //assert
        assertTrue(result.isPresent());
        assertEquals(provider.getId(),result.get().getId());
        assertEquals(provider.getProvider().getLabel(),result.get().getProvider().getLabel());
    }

    @Test
    void getProviderByType_shouldReturnEmpty_whenProviderDoesNotExists(){
        //act
        Optional<Provider> result = providerDao.getProviderByType(Providers.BASIC);

        //assert
        assertTrue(result.isEmpty());
    }
}
