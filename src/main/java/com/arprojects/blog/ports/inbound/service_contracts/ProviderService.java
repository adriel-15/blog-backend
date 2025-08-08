package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.ProviderDto;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;

public interface ProviderService {

    ProviderDto getByType(Providers providerType) throws ProviderNotFoundException;

}
