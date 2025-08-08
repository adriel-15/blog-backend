package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.AuthorityDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorityServiceImplUnitTest {

    @Mock
    private AuthorityDao authorityDao;

    @InjectMocks
    private AuthorityServiceImpl authorityService;

    @Test
    void getByType_shouldReturnAuthorityDto_ifAuthorityTypeExists() throws AuthorityNotFoundException {
        //arrange
        Authority authority = new Authority(Authorities.READER);
        authority.setId(1);

        when(authorityDao.getAuthorityByType(any(Authorities.class))).thenAnswer(invocation -> {
            Authorities authorityType = invocation.getArgument(0);
            boolean exists = authority.getAuthority().getLabel().equals(authorityType.getLabel());
            return exists ? Optional.of(authority) : Optional.empty();
        });

        AuthorityDto authorityDto = authorityService.getByType(Authorities.READER);

        assertEquals(authority.getId(),authorityDto.id());
        assertEquals(authority.getAuthority(),authorityDto.authorityType());
    }

    @Test
    void getByType_shouldThrowAuthorityNotFoundException_ifAuthorityTypeDoesNotExists() throws AuthorityNotFoundException {
        //arrange
        Authority authority = new Authority(Authorities.READER);
        authority.setId(1);

        when(authorityDao.getAuthorityByType(any(Authorities.class))).thenAnswer(invocation -> {
            Authorities authorityType = invocation.getArgument(0);
            boolean exists = authority.getAuthority().getLabel().equals(authorityType.getLabel());
            return exists ? Optional.of(authority) : Optional.empty();
        });

        assertThrows(AuthorityNotFoundException.class, () -> authorityService.getByType(Authorities.WRITER));
    }

}
