package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.AuthorityDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.ports.inbound.service_contracts.AuthorityService;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
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
public class AuthorityServiceImplIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @MockitoSpyBean
    private AuthorityDao authorityDao;

    @Autowired
    private AuthorityService authorityService;

    @BeforeEach
    void setUp(){
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        entityManager.persist(authority);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void getByType_shouldReturnAuthorityDto_ifAuthorityTypeExists() throws AuthorityNotFoundException {
        Authorities authorityType = Authorities.ADMIN;

        //first call should hit the database
        AuthorityDto first = authorityService.getByType(authorityType);

        //second call should hit the cache
        AuthorityDto second = authorityService.getByType(authorityType);

        assertEquals(authorityType,second.authorityType());

        verify(authorityDao, times(1)).getAuthorityByType(authorityType);
    }

    @Test
    @Transactional
    void getByType_shouldThrowAuthorityNotFoundException_ifAuthorityTypeDoesNotExists(){
        Authorities authorityType = Authorities.READER;

        assertThrows(AuthorityNotFoundException.class,() -> authorityService.getByType(authorityType));

    }
}
