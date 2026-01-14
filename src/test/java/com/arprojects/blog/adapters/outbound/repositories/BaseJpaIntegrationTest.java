package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        ProviderDaoJpaImpl.class,
        AuthorityDaoJpaImpl.class,
        UserDaoJpaImpl.class
})
@ActiveProfiles("test")
abstract class BaseJpaIntegrationTest {

    static final MySQLContainer<?> mysql;

    static {
        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("arblog_test")
                .withUsername("test")
                .withPassword("test");
        mysql.start();
    }

    @DynamicPropertySource
    static void registerAwsProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    protected AuthorityDao authorityDao;

    @Autowired
    protected ProviderDao providerDao;

    @Autowired
    protected UserDao userDao;

    @BeforeEach
    void baseSetUp(){
        authorityDao.deleteAll();
        providerDao.deleteAll();
        userDao.deleteAll();
    }

// ----------- helper methods --------------
    protected void seedAuthority() {
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        authorityDao.save(authority);
    }

    protected void seedProvider(){
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        providerDao.save(provider);
    }

    protected void seeDefaultUser(){
        seedAuthority();
        seedProvider();

        Profile profile = new Profile();
        profile.setProfileName("john-doe-123");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));
        profile.setCreatedAt(LocalDateTime.now());

        Provider provider = providerDao.getByType(Providers.BASIC)
                .orElseThrow(() -> new IllegalStateException("Provider BASIC missing"));

        Authority authority = authorityDao.getByType(Authorities.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Authority ADMIN missing"));

        //create and persist user
        User user = new User();
        user.setUsername("johnDoe");
        user.setPassword("test123");
        user.setEmail("johnDoe@gmail.com");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setProviderUniqueId("1234");
        user.setProfile(profile);
        user.setProvider(provider);
        user.setAuthorities(Set.of(authority));

        userDao.save(user);
    }

}
