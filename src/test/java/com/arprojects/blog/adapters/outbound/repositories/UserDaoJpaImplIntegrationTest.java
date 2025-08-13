package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.*;
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
class UserDaoJpaImplIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserDao userDao;

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
    void getUserByUsername_returnsUser_ifUserExists() {
        Optional<User> result = userDao.getUserByUsername("adriel15");

        assertTrue(result.isPresent());
        assertTrue(result.get().isEnabled());
        assertNotNull(result.get().getCreatedAt());
        assertNotNull(result.get().getUpdatedAt());

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
    void getUserByUsername_returnsEmpty_ifUserDoNotExists() {
        Optional<User> result = userDao.getUserByUsername("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void getUserByProviderUID_returnsUser_ifUserExists() {
        Optional<User> result = userDao.getUserByProviderUID("provider-unique-id-mock");

        assertTrue(result.isPresent());
        assertEquals("adriel15", result.get().getUsername());
        assertEquals("adrielTest@gmail.com", result.get().getEmail());
        assertEquals("provider-unique-id-mock", result.get().getProviderUniqueId());
    }

    @Test
    @Transactional
    void getUserByProviderUID_returnsEmpty_ifUserDoesNotExist() {
        Optional<User> result = userDao.getUserByProviderUID("non-existent-id");

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void create_shouldPersistUserSuccessfully() {
        Authority authority = entityManager.createQuery("SELECT a FROM Authority a WHERE a.authorityType = :auth", Authority.class)
                .setParameter("auth", Authorities.ADMIN)
                .getSingleResult();

        Provider provider = entityManager.createQuery("SELECT p FROM Provider p WHERE p.providerType = :prov", Provider.class)
                .setParameter("prov", Providers.BASIC)
                .getSingleResult();

        Profile profile = new Profile();
        profile.setProfileName("new-profile-name");
        profile.setBirthDate(LocalDate.of(1999, Month.JANUARY, 1));

        User user = new User();
        user.setUsername("newuser");
        user.setPassword("newpass");
        user.setEmail("newuser@example.com");
        user.setProviderUniqueId("new-provider-id");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setProvider(provider);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));

        userDao.create(user);
        entityManager.flush();

        User persisted = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", "newuser")
                .getSingleResult();

        assertNotNull(persisted);
        assertEquals("newuser@example.com", persisted.getEmail());
    }

    @Test
    @Transactional
    void emailExists_shouldReturnTrue_ifEmailExists(){
        assertTrue(userDao.emailExists("adrielTest@gmail.com"));
    }

    @Test
    @Transactional
    void emailExists_shouldReturnFalse_ifEmailDoesNotExists(){
        assertFalse(userDao.emailExists("unknown@gmail.com"));
    }

    @Test
    @Transactional
    void providerUIDExists_shouldReturnTrue_ifProviderUIDExists(){
        assertTrue(userDao.providerUIDExists("provider-unique-id-mock"));
    }

    @Test
    @Transactional
    void providerUIDExists_shouldReturnFalse_ifProviderUIDDoesNotExists(){
        assertFalse(userDao.providerUIDExists("unknown-provider"));
    }

    @Test
    @Transactional
    void usernameExists_shouldReturnTrue_ifUsernameExists(){
        assertTrue(userDao.usernameExists("adriel15"));
    }

    @Test
    @Transactional
    void usernameExists_shouldReturnFalse_ifUsernameExists(){
        assertFalse(userDao.usernameExists("unknown"));
    }
}
