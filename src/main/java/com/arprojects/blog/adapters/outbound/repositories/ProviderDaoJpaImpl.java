package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ProviderDaoJpaImpl implements ProviderDao{

    private final EntityManager entityManager;

    @Autowired
    public ProviderDaoJpaImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Provider> getByType(Providers providerType) {
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

    @Override
    @Transactional
    public void save(Provider provider) {
        entityManager.persist(provider);
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("delete from Provider").executeUpdate();
    }
}
