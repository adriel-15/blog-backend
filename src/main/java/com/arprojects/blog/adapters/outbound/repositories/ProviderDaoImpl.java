package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProviderDaoImpl implements ProviderDao {

    private final EntityManager entityManager;

    @Autowired
    public ProviderDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }
    @Override
    public Optional<Provider> getProviderByType(Providers providerType) {
        String query = "from Provider where providerType=:providerType";
        try{
            Provider provider = entityManager.createQuery(query,Provider.class)
                    .setParameter("providerType",providerType)
                    .getSingleResult();

            return Optional.of(provider);
        }catch (Exception ex){
            return Optional.empty();
        }
    }
}
