package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
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
public class AuthorityDaoImplUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Authority> typedQuery;

    @InjectMocks
    private AuthorityDaoImpl authorityDao;

    @Test
    void getAuthorityByType_shouldReturnAuthority_whenTypeExists(){
        //arrange
        Authority authority = new Authority(Authorities.READER);
        authority.setId(1);

        String query = "from Authority where authorityType=:authorityType";

        when(entityManager.createQuery(query,Authority.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("authorityType",Authorities.READER)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(authority);

        //act
        Optional<Authority> result = authorityDao.getAuthorityByType(Authorities.READER);

        //assert
        assertTrue(result.isPresent());
        assertEquals(authority.getId(),result.get().getId());
        assertEquals(authority.getAuthority().getLabel(),result.get().getAuthority().getLabel());
    }

    @Test
    void getAuthorityByType_shouldReturnEmptyOptional_whenTypeDoesNotExists(){
        //act
        Optional<Authority> result = authorityDao.getAuthorityByType(Authorities.READER);

        //assert
        assertTrue(result.isEmpty());
    }
}
