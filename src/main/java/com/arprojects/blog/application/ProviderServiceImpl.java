package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.ProviderDto;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.ports.inbound.service_contracts.ProviderService;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderDao providerDao;

    public ProviderServiceImpl(ProviderDao providerDao){
        this.providerDao = providerDao;
    }
    @Override
    @Cacheable(value = "providerByType", key = "#providerType")
    public ProviderDto getByType(Providers providerType) throws ProviderNotFoundException {

        Provider provider = providerDao.getProviderByType(providerType)
                .orElseThrow(() -> new ProviderNotFoundException("Provider does not exists"));

        return mapFromProviderToProviderDto.apply(provider);
    }

    Function<Provider,ProviderDto> mapFromProviderToProviderDto = provider -> {
        return new ProviderDto(provider.getId(),provider.getProvider());
    };
}
