package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class CustomUserDetailServiceIntegrationTest extends BaseServiceIntegrationTest{

    @Test
    @DisplayName("load user by username return user details.")
    void loadByUsername_returnUserDetails(){
        seeDefaultUser();

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername("johnDoe");

        assertEquals("john-doe-123",userDetails.getProfileName());
        assertEquals("johnDoe",userDetails.getUsername());
    }

    @Test
    @DisplayName("load user by username throw UserNotFoundException")
    void loadByUsername_throwUserNotFoundException(){
        assertThrows(UsernameNotFoundException.class, () ->  customUserDetailService.loadUserByUsername("unknown"));
    }
}
