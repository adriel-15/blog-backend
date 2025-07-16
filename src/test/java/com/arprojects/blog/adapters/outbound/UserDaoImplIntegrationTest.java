package com.arprojects.blog.adapters.outbound;

import com.arprojects.blog.adapters.outbound.repositories.UserDaoImpl;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDaoImplIntegrationTest {

    private UserDao userDao;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp(){
        // Inject the real implementation
        userDao = new UserDaoImpl(entityManager);

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
    void testGetUserByUsername_returnsUser_ifUserExists() {
        Optional<User> result = userDao.getUserByUsername("adriel15");

        assertTrue(result.isPresent());
        assertTrue(result.get().isEnabled());
        assertNotNull(result.get().getCreatedAt());
        assertNull(result.get().getUpdatedAt());

        assertEquals("adriel15", result.get().getUsername());
        assertEquals("test123", result.get().getPassword());
        assertEquals("adrielTest@gmail.com", result.get().getEmail());
        assertEquals(1, result.get().getAuthorities().size());
        assertEquals(Authorities.ADMIN, result.get().getAuthorities().iterator().next().getAuthority());
        assertEquals(Providers.BASIC, result.get().getProvider().getProvider());
        assertEquals("adriel-rosario15", result.get().getProfile().getProfileName());
        assertEquals(LocalDate.of(2000,Month.SEPTEMBER,15),result.get().getProfile().getBirthDate());
        assertEquals("provider-unique-id-mock",result.get().getProviderUniqueId());
    }

    @Test
    @Transactional
    void testGetUserByUsername_returnsEmpty_ifUserDoNotExists() {
        Optional<User> result = userDao.getUserByUsername("unknown");

        assertTrue(result.isEmpty());
    }
}
