package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.CustomUserDetails;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CustomUserDetailServiceIntegrationTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CustomUserDetailService customUserDetailService;

    @BeforeEach
    void setUp(){
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        entityManager.persist(authority);

        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        entityManager.persist(provider);

        //create and persist Profile
        Profile profile = new Profile();
        profile.setProfileName("adriel-rosario15");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));
        entityManager.persist(profile);

        //create and persist user
        User user = new User();
        user.setUsername("adriel15");
        user.setPassword("test123");
        user.setEmail("adrielTest@gmail.com");
        user.setProviderUniqueId("provider-unique-id-mock");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setProvider(provider);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists(){
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername("adriel15");

        assertEquals("adriel-rosario15",userDetails.getProfileName());
        assertEquals("adriel15",userDetails.getUsername());
    }

    @Test
    @Transactional
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserDoesNotExists(){
        assertThrows(UsernameNotFoundException.class, () ->  customUserDetailService.loadUserByUsername("unknown"));
    }
}
