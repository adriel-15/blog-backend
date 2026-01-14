package com.arprojects.blog.application;

import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.ports.inbound.service_contracts.AuthorityService;
import com.arprojects.blog.ports.inbound.service_contracts.ProviderService;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import com.arprojects.blog.ports.outbound.repository_contracts.ProviderDao;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Objects;
import java.util.Set;

abstract class BaseServiceIntegrationTest {

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
    protected AuthorityService authorityService;

    @Autowired
    protected ProviderService providerService;

    @Autowired
    protected CustomUserDetailService customUserDetailService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected CacheManager cacheManager;

    @MockitoSpyBean
    protected AuthorityDao authorityDao;

    @MockitoSpyBean
    protected ProviderDao providerDao;

    @MockitoSpyBean
    protected UserDao userDao;

    @BeforeEach
    void setup(){
        clearCaches();
        clearAll();
    }

    // ------------ helpers methods ---------
    protected void clearCaches(){
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    protected void clearAll(){
        userDao.deleteAll();
        authorityDao.deleteAll();
        providerDao.deleteAll();
    }

    protected void seedAuthorities(){
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        authorityDao.save(authority);

        // Add these if they're needed for multiple tests
        Authority readerAuthority = new Authority();
        readerAuthority.setAuthority(Authorities.READER);
        authorityDao.save(readerAuthority);
    }

    protected void seedProviders(){
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        providerDao.save(provider);

        Provider googleProvider = new Provider();
        googleProvider.setProvider(Providers.GOOGLE);
        providerDao.save(googleProvider);
    }

    protected void seeDefaultUser(){
        seedAuthorities();
        seedProviders();

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
