package com.arprojects.blog.application;


import com.arprojects.blog.domain.dtos.AuthorityDto;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class AuthorityServiceIntegrationTest extends BaseServiceIntegrationTest{

    @Test
    @DisplayName("getByType(Enum authority) - should return authorityDto if exists.")
    void getByType_returnAuthorityDto() throws AuthorityNotFoundException {
        seedAuthorities();

        //first call should hit the database
        AuthorityDto first = authorityService.getByType(Authorities.ADMIN);

        //second call should hit the cache
        AuthorityDto second = authorityService.getByType(Authorities.ADMIN);

        assertEquals(Authorities.ADMIN,first.authorityType());
        assertEquals(Authorities.ADMIN,second.authorityType());
        verify(authorityDao, times(1)).getByType(Authorities.ADMIN);
    }

    @Test
    @DisplayName("getByType(Enum authority) - should throw AuthorityNotFoundException.")
    void getByType_throwException(){
        assertThrows(AuthorityNotFoundException.class,() -> authorityService.getByType(Authorities.READER));
    }

}
