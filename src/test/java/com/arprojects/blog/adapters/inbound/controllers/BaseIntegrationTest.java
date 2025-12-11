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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import java.util.Set;

public class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("arblog_test")
            .withUsername("test")
            .withPassword("test");

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

    // ------ helper methods -------
    protected void clearCaches(){
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    protected void seedAuthorities(){
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.ADMIN);
        authorityDao.create(authority);

        // Add these if they're needed for multiple tests
        Authority readerAuthority = new Authority();
        readerAuthority.setAuthority(Authorities.READER);
        authorityDao.create(readerAuthority);
    }

    protected void seedProviders(){
        Provider provider = new Provider();
        provider.setProvider(Providers.BASIC);
        providerDao.create(provider);

        Provider googleProvider = new Provider();
        googleProvider.setProvider(Providers.GOOGLE);
        providerDao.create(googleProvider);
    }

    protected void seedDefaultUser(){
        Profile profile = new Profile();
        profile.setProfileName("adriel-rosario15");
        profile.setBirthDate(LocalDate.of(2000, Month.SEPTEMBER,15));

        var provider = providerDao.getProviderByType(Providers.BASIC);
        var adminAuthority = authorityDao.getAuthorityByType(Authorities.ADMIN);

        //create and persist user
        User user = new User();
        user.setUsername("adriel15Rosario123");
        user.setPassword(encoder.encode("test123"));
        user.setEmail("adrielTest@gmail.com");
        user.setEnabled(true);
        user.setProfile(profile);

        provider.ifPresent(user::setProvider);
        adminAuthority.ifPresent(authority -> user.setAuthorities(Set.of(authority)));

        userDao.create(user);
    }

    protected void seedGoogleUser(){

        Profile profile = new Profile();
        profile.setProfileName("John Doe");

        var provider = providerDao.getProviderByType(Providers.GOOGLE);
        var readerAuthority = authorityDao.getAuthorityByType(Authorities.READER);

        User user = new User();
        user.setEmail("test@example.com");
        user.setProviderUniqueId("123");
        user.setEnabled(true);
        user.setProfile(profile);

        if(provider.isEmpty())
            System.out.println("Provider is NULL!!!");

        provider.ifPresent(user::setProvider);
        readerAuthority.ifPresent(auth -> user.setAuthorities(Set.of(auth)));


        userDao.create(user);
    }

    protected void deleteAll(){
        userDao.deleteAll();
        providerDao.deleteAll();
        authorityDao.deleteAll();
    }
}
