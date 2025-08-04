package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
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
public class AuthorityDaoImplIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AuthorityDao authorityDao;

    @BeforeEach
    void setup(){
        //create and persist Authority
        Authority authority = new Authority(Authorities.READER);
        entityManager.persist(authority);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void getAuthorityByType_shouldReturnAuthority_whenTypeExists(){
        Optional<Authority> result = authorityDao.getAuthorityByType(Authorities.READER);

        assertTrue(result.isPresent());
        assertEquals(Authorities.READER.getLabel(),result.get().getAuthority().getLabel());
    }

    @Test
    @Transactional
    void getAuthorityByType_shouldReturnEmptyOptional_whenTypeDoesNotExists(){
        Optional<Authority> result = authorityDao.getAuthorityByType(Authorities.WRITER);

        assertTrue(result.isEmpty());
    }
}
