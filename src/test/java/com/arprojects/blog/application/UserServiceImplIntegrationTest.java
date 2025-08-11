package com.arprojects.blog.application;


import com.arprojects.blog.domain.dtos.AddGoogleUserDto;
import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.dtos.UserDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplIntegrationTest {

    @MockitoSpyBean
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp(){

        cacheManager.getCache("authorityByType").clear();
        cacheManager.getCache("providerByType").clear();
        cacheManager.getCache("usersByProviderUID").clear();
        cacheManager.getCache("providerUIDExists").clear();
        cacheManager.getCache("emailExists").clear();

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
        user.setProvider(provider);
        user.setProfile(profile);
        user.setAuthorities(Set.of(authority));
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();
    }
    @Test
    @Transactional
    void emailExists_returnTrue_ifExists(){
        String email = "adrielTest@gmail.com";

        //first call should hit the database
        boolean first = userService.emailExists(email);

        //second call should hit the cache
        boolean second = userService.emailExists(email);
        assertTrue(second);

        // Verify DAO is only called once
        verify(userDao, times(1)).emailExists(email);
    }

    @Test
    @Transactional
    void emailExists_returnFalse_ifNotExists(){
        boolean exists = userService.emailExists("notexists@gmail.com");
        assertFalse(exists);
    }

    @Test
    @Transactional
    void providerUIDExists_returnTrue_ifExists(){
        String providerUID = "provider-unique-id-mock";

        //first call should hit the database
        boolean first = userService.providerUIDExists(providerUID);

        //second call should hit the cache
        boolean second = userService.providerUIDExists(providerUID);
        assertTrue(second);

        //verify dao is only call once
        verify(userDao, times(1)).providerUIDExists(providerUID);
    }

    @Test
    @Transactional
    void providerUIDExists_returnFalse_ifNotExists(){
        boolean exists =userService.providerUIDExists("invalid-provider-uid");
        assertFalse(exists);
    }

    @Test
    @Transactional
    void getUserByProviderUID_returnUserDto_ifProviderUIDValid() throws UserNotFoundException {
        String providerUID = "provider-unique-id-mock";

        //first call should hit the database
        UserDto first = userService.getByProviderUID(providerUID);

        //second call should hit the cache
        UserDto second = userService.getByProviderUID(providerUID);

        verify(userDao,times(1)).getUserByProviderUID(providerUID);

        assertEquals("adrielTest@gmail.com",second.email());
        assertEquals(Providers.BASIC,second.provider());
    }

    @Test
    @Transactional
    void getUserByProviderUID_throwUserNotFoundException_ifProviderUIDInvalid() throws UserNotFoundException {
        assertThrows(UserNotFoundException.class,() -> userService.getByProviderUID("invalid-provider"));
    }

    @Test
    @Transactional
    void addGoogleUser_returnUserDto_ifSuccessfullyCreate() throws ProviderNotFoundException, AuthorityNotFoundException {
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.GOOGLE);
        entityManager.persist(provider);

        entityManager.flush();
        entityManager.clear();

        //arrange
        AddGoogleUserDto addGoogleUserDto = new AddGoogleUserDto(
                "test@gmail.com",
                "12343243342",
                true,
                "Adriel Rosario Sanchez"
        );

        UserDto userDto = userService.addGoogleUser(addGoogleUserDto);

        assertEquals(addGoogleUserDto.profileName(),userDto.profile().profileName());
        assertEquals(addGoogleUserDto.email(),userDto.email());
        assertTrue(userDto.id() > 0);
    }

    @Test
    @Transactional
    void addGoogleUser_throwAuthorityNotFoundException_ifAuthorityDoesNotExist() throws ProviderNotFoundException, AuthorityNotFoundException {
        //create and persist Provider
        Provider provider = new Provider();
        provider.setProvider(Providers.GOOGLE);
        entityManager.persist(provider);

        entityManager.flush();
        entityManager.clear();

        //arrange
        AddGoogleUserDto addGoogleUserDto = new AddGoogleUserDto(
                "test3@gmail.com",
                "12343243342asdfasdfas",
                true,
                "Alex Rosario Sanchez"
        );

        assertThrows(AuthorityNotFoundException.class, () -> userService.addGoogleUser(addGoogleUserDto));
    }

    @Test
    @Transactional
    void addGoogleUser_throwProviderNotFoundException_ifProviderDoesNotExist() throws ProviderNotFoundException, AuthorityNotFoundException {
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        entityManager.flush();
        entityManager.clear();

        //arrange
        AddGoogleUserDto addGoogleUserDto = new AddGoogleUserDto(
                "test2@gmail.com",
                "123432433421231231",
                true,
                "Adonis Rosario Sanchez"
        );

        assertThrows(ProviderNotFoundException.class, () -> userService.addGoogleUser(addGoogleUserDto));
    }

    @Test
    @Transactional
    void add_createUser_ifSignUpDtoIsValid() throws AuthorityNotFoundException, ProviderNotFoundException, UsernameAlreadyExistsException, EmailAlreadyExistsException{
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        SignUpDto signUpDto = new SignUpDto(
                "alex19",
                "alex19rosario@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        userService.add(signUpDto);

        verify(userDao,times(1)).create(any());
    }

    @Test
    @Transactional
    void add_throwEmailAlreadyExistsException_ifSignUpDtoContainsDuplicateEmail() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException{
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        SignUpDto signUpDto = new SignUpDto(
                "alex19",
                "adrielTest@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        assertThrows(EmailAlreadyExistsException.class, () -> userService.add(signUpDto));
    }

    @Test
    @Transactional
    void add_throwUsernameAlreadyExistsException_ifSignUpDtoContainsDuplicateUsername() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException{
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        SignUpDto signUpDto = new SignUpDto(
                "adriel15",
                "adrielTest123@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.add(signUpDto));
    }

    @Test
    @Transactional
    void add_receiveSignUpDto_throwAuthorityNotFoundException_ifAuthorityIsNotValid() throws AuthorityNotFoundException{
        SignUpDto signUpDto = new SignUpDto(
                "adriel1523",
                "adrielTest123423@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        assertThrows(AuthorityNotFoundException.class, () -> userService.add(signUpDto));
    }

    @Test
    @Transactional
    void add_receiveSignUpDto_throwProviderNotFoundException_ifProviderIsNotValid() throws ProviderNotFoundException, AuthorityNotFoundException{
        User user = entityManager.createQuery("from User where username=:username", User.class)
                .setParameter("username","adriel15")
                .getSingleResult();

        entityManager.remove(user);

        Provider provider = entityManager.createQuery("from Provider where providerType=:providerType",Provider.class)
               .setParameter("providerType",Providers.BASIC)
               .getSingleResult();

        entityManager.remove(provider);

        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        SignUpDto signUpDto = new SignUpDto(
                "adriel1523",
                "adrielTest12343@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        assertThrows(ProviderNotFoundException.class, () -> userService.add(signUpDto));

    }

    @Test
    @Transactional
    void usernameExists_returnTrue_ifExists(){
        String username = "adriel15";

        //first call should hit database
        boolean first = userService.usernameExists(username);

        //second call should hit cache
        boolean second = userService.usernameExists(username);
        assertTrue(second);

        //verify dao is only call once
        verify(userDao,times(1)).usernameExists(username);

    }

    @Test
    @Transactional
    void usernameExists_returnFalse_ifNotExists(){
        boolean exists = userService.usernameExists("unknown-username");
        assertFalse(exists);
    }

}
