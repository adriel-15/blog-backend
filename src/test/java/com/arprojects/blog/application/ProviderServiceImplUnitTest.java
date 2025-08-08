package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.ProviderDto;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProviderServiceImplUnitTest {

    @Mock
    private ProviderDao providerDao;

    @InjectMocks
    private ProviderServiceImpl providerService;

    @Test
    void getByType_shouldReturnProviderDto_ifProviderTypeExists() throws ProviderNotFoundException {
        Provider provider = new Provider(Providers.BASIC);
        provider.setId(1);

        when(providerDao.getProviderByType(any(Providers.class))).thenAnswer(invocation -> {
            Providers providerType = invocation.getArgument(0);
            boolean exists = provider.getProvider().getLabel().equals(providerType.getLabel());
            return exists? Optional.of(provider) : Optional.empty();
        });

        ProviderDto providerDto = providerService.getByType(Providers.BASIC);

        assertEquals(provider.getId(),providerDto.id());
        assertEquals(provider.getProvider(),providerDto.provider());
    }

    @Test
    void getByType_shouldThrowProviderNotFoundException_ifProviderTypeDoesNotExists(){
        Provider provider = new Provider(Providers.BASIC);
        provider.setId(1);

        when(providerDao.getProviderByType(any(Providers.class))).thenAnswer(invocation -> {
            Providers providerType = invocation.getArgument(0);
            boolean exists = provider.getProvider().getLabel().equals(providerType.getLabel());
            return exists? Optional.of(provider) : Optional.empty();
        });

        assertThrows(ProviderNotFoundException.class, () -> providerService.getByType(Providers.GOOGLE));
    }
}
