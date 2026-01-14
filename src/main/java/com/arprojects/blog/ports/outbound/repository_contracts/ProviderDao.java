package com.arprojects.blog.ports.outbound.repository_contracts;

import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;

import java.util.Optional;

public interface ProviderDao {

    Optional<Provider> getByType(Providers providers);

    void save(Provider provider);

    void deleteAll();
}
