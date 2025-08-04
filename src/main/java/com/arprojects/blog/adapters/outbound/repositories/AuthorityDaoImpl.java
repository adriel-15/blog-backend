package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthorityDaoImpl implements AuthorityDao {

    private EntityManager entityManager;

    @Autowired
    public AuthorityDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Authority> getAuthorityByType(Authorities authorityType) {

        String query = "from Authority where authorityType=:authorityType";

        try{
            Authority authority = entityManager.createQuery(query,Authority.class)
                    .setParameter("authorityType",authorityType)
                    .getSingleResult();

            return Optional.of(authority);
        }catch (Exception ex){
            return Optional.empty();
        }
    }
}
