package com.arprojects.blog.adapters.inbound.controllers;

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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.Set;


public abstract class ControllerBaseIntegrationTest {

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

    //class fields
    @LocalServerPort
    protected int port;

    protected RestTestClient client;

    @Autowired
    protected PasswordEncoder encoder;

    @Autowired
    protected JwtDecoder jwtDecoder;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected AuthorityDao authorityDao;

    @Autowired
    protected ProviderDao providerDao;

    @Autowired
    protected UserDao userDao;

    @BeforeEach
    void setup(){
        client = RestTestClient.bindToServer().baseUrl("http://localhost:"+port).build();

        clearCaches();
        deleteAll();

        seedAuthorities();
        seedProviders();
    }

    // ------ helper methods -------
    protected void clearCaches(){
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
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

    protected void seedDefaultUser(){
        Profile profile = new Profile();
        profile.setProfileName("adriel-rosario15");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));

        Provider provider = providerDao.getByType(Providers.BASIC)
                .orElseThrow(() -> new IllegalStateException("Provider BASIC missing"));

        Authority authority = authorityDao.getByType(Authorities.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Authority ADMIN missing"));

        //create and persist user
        User user = new User();
        user.setUsername("adriel15Rosario123");
        user.setPassword(encoder.encode("test123"));
        user.setEmail("adrielTest@gmail.com");
        user.setEnabled(true);
        user.setProfile(profile);
        user.setProvider(provider);
        user.setAuthorities(Set.of(authority));

        userDao.save(user);
    }

    protected void seedGoogleUser(){

        Profile profile = new Profile();
        profile.setProfileName("John Doe");

        Provider provider = providerDao.getByType(Providers.GOOGLE)
                .orElseThrow(() -> new IllegalStateException("Provider GOOGLE missing"));

        Authority authority = authorityDao.getByType(Authorities.READER)
                .orElseThrow(() -> new IllegalStateException("Authority Reader missing"));

        User user = new User();
        user.setEmail("test@example.com");
        user.setProviderUniqueId("123");
        user.setEnabled(true);
        user.setProfile(profile);
        user.setProvider(provider);
        user.setAuthorities(Set.of(authority));

        userDao.save(user);
    }

    protected void deleteAll(){
        userDao.deleteAll();
        providerDao.deleteAll();
        authorityDao.deleteAll();
    }
}
