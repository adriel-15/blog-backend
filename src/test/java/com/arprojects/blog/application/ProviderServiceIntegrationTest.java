package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.ProviderDto;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
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
class ProviderServiceIntegrationTest extends BaseServiceIntegrationTest{

    @Test
    @DisplayName("getByType(Enum provider) - should return ProviderDto if exists.")
    void getByType_returnProviderDto() throws ProviderNotFoundException {
        seedProviders();

        //first call should hit the database
        ProviderDto first = providerService.getByType(Providers.BASIC);

        //second call should hit the cache
        ProviderDto second = providerService.getByType(Providers.BASIC);

        assertEquals(Providers.BASIC,first.provider());
        assertEquals(Providers.BASIC,second.provider());
        verify(providerDao,times(1)).getByType(Providers.BASIC);
    }

    @Test
    @DisplayName("getByType(Enum provider) - should throw ProviderNotFoundException.")
    void getByType_throwProviderNotFoundException(){
        assertThrows(ProviderNotFoundException.class, () -> providerService.getByType(Providers.BASIC));
    }

}
