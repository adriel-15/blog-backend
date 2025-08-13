package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.*;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.AuthorityService;
import com.arprojects.blog.ports.inbound.service_contracts.ProviderService;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTest {

    @Mock
    private UserDao userDao;

    @Mock
    private AuthorityDao authorityDao;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private ProviderService providerService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void emailExists_returnTrue_ifExists(){
        String validEmail = "adrielRosario@gmail.com";

        when(userDao.emailExists(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return validEmail.equals(email);
        });

        assertTrue(userService.emailExists(validEmail));
    }

    @Test
    void emailExists_returnFalse_ifNotExists(){
        String validEmail = "adrielRosario@gmail.com";

        when(userDao.emailExists(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return validEmail.equals(email);
        });

        assertFalse(userService.emailExists("unknownEmail"));
    }

    @Test
    void providerUIDExists_returnTrue_ifExists(){
        String validProviderUID = "1234124234124";

        when(userDao.providerUIDExists(anyString())).thenAnswer(invocation -> {
            String providerUID = invocation.getArgument(0);
            return validProviderUID.equals(providerUID);
        });

        assertTrue(userService.providerUIDExists(validProviderUID));
    }

    @Test
    void providerUIDExists_returnFalse_ifNotExists(){
        String validProviderUID = "1234124234124";

        when(userDao.providerUIDExists(anyString())).thenAnswer(invocation -> {
            String providerUID = invocation.getArgument(0);
            return validProviderUID.equals(providerUID);
        });

        assertFalse(userService.providerUIDExists("unknown"));
    }

    @Test
    void getUserByProviderUID_returnUserDto_ifProviderUIDValid() throws UserNotFoundException {
        //arrange
        String validProviderUID = "provider-uid";
        Profile profile = new Profile();
        profile.setProfileName("adriel");
        profile.setBirthDate(LocalDate.now());
        profile.setId(1);
        User user = new User();
        user.setId(1);
        user.setEmail("test@gmail.com");
        user.setProviderUniqueId(validProviderUID);
        user.setAuthorities(Set.of(new Authority(Authorities.READER)));
        user.setProfile(profile);
        user.setProvider(new Provider(Providers.GOOGLE));

        when(userDao.getUserByProviderUID(anyString())).thenAnswer(invocation -> {
            String providerUID = invocation.getArgument(0);
            boolean exists = validProviderUID.equals(providerUID);
            return exists ? Optional.of(user):Optional.empty();
        });

        UserDto userdto = userService.getByProviderUID(validProviderUID);

        assertEquals(user.getId(), userdto.id());
        assertEquals(user.getEmail(), userdto.email());
    }

    @Test
    void getUserByProviderUID_throwUserNotFoundException_ifProviderUIDInvalid(){
        //arrange
        String validProviderUID = "provider-uid";
        Profile profile = new Profile();
        profile.setProfileName("adriel");
        profile.setBirthDate(LocalDate.now());
        profile.setId(1);
        User user = new User();
        user.setId(1);
        user.setEmail("test@gmail.com");
        user.setProviderUniqueId(validProviderUID);
        user.setAuthorities(Set.of(new Authority(Authorities.READER)));
        user.setProfile(profile);
        user.setProvider(new Provider(Providers.GOOGLE));

        when(userDao.getUserByProviderUID(anyString())).thenAnswer(invocation -> {
            String providerUID = invocation.getArgument(0);
            boolean exists = validProviderUID.equals(providerUID);
            return exists ? Optional.of(user):Optional.empty();
        });

        assertThrows(UserNotFoundException.class, () -> userService.getByProviderUID("unknown-uid"));
    }

    @Test
    void addGoogleUser_returnUserDto_ifSuccessfullyCreate() throws AuthorityNotFoundException, ProviderNotFoundException {
        //arrange
        AddGoogleUserDto addGoogleUserDto = new AddGoogleUserDto(
                "test@gmail.com",
                "12343243342",
                true,
                "Adriel Rosario"
        );

        AuthorityDto validAuthorityDto = new AuthorityDto(1,Authorities.READER);
        ProviderDto validProviderDto = new ProviderDto(1,Providers.BASIC);

        when(authorityService.getByType(any(Authorities.class))).thenReturn(validAuthorityDto);

        when(providerService.getByType(any(Providers.class))).thenReturn(validProviderDto);

        Profile profile = new Profile();
        profile.setProfileName(addGoogleUserDto.profileName());

        User user = new User();
        user.setEmail(addGoogleUserDto.email());
        user.setProviderUniqueId(addGoogleUserDto.providerUID());
        user.setEnabled(true);
        user.setProfile(profile);
        user.setAuthorities(Set.of(userService.mapFromAuthorityDtoToAuthority.apply(validAuthorityDto)));
        user.setProvider(userService.mapFromProviderDtoToProvider.apply(validProviderDto));

        doAnswer(invocation -> {
            User userFromArgs = invocation.getArgument(0);
            userFromArgs.setId(1);
            return null;
        }).when(userDao).create(any(User.class));

        UserDto result = userService.addGoogleUser(addGoogleUserDto);

        assertEquals(1,result.id());
        assertEquals(user.getEmail(),result.email());
        assertEquals(user.getProfile().getProfileName(),result.profile().profileName());
    }

    @Test
    void addGoogleUser_throwAuthorityNotFoundException_ifAuthorityDoesNotExist() throws AuthorityNotFoundException {
        //arrange
        AddGoogleUserDto addGoogleUserDto = new AddGoogleUserDto(
                "test@gmail.com",
                "12343243342",
                true,
                "Adriel Rosario"
        );

        when(authorityService.getByType(any(Authorities.class))).thenThrow(new AuthorityNotFoundException("not found"));

        assertThrows(AuthorityNotFoundException.class,() -> userService.addGoogleUser(addGoogleUserDto));
    }

    @Test
    void addGoogleUser_throwProviderNotFoundException_ifProviderDoesNotExist() throws AuthorityNotFoundException, ProviderNotFoundException {
        //arrange
        AddGoogleUserDto addGoogleUserDto = new AddGoogleUserDto(
                "test@gmail.com",
                "12343243342",
                true,
                "Adriel Rosario"
        );

        AuthorityDto validAuthorityDto = new AuthorityDto(1,Authorities.READER);

        when(authorityService.getByType(any(Authorities.class))).thenReturn(validAuthorityDto);

        when(providerService.getByType(any(Providers.class))).thenThrow(new ProviderNotFoundException("not found"));

        assertThrows(ProviderNotFoundException.class,() -> userService.addGoogleUser(addGoogleUserDto));
    }

    //new tests
    @Test
    void add_createUser_ifSignUpDtoIsValid() throws AuthorityNotFoundException, ProviderNotFoundException, UsernameAlreadyExistsException, EmailAlreadyExistsException {
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adriel15rosario@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        AuthorityDto authorityDto = new AuthorityDto(1,Authorities.READER);
        ProviderDto providerDto = new ProviderDto(1,Providers.BASIC);

        when(userDao.usernameExists(anyString())).thenReturn(false);
        when(userDao.emailExists(anyString())).thenReturn(false);
        when(authorityService.getByType(any(Authorities.class))).thenReturn(authorityDto);
        when(providerService.getByType(any(Providers.class))).thenReturn(providerDto);

        userService.add(signUpDto);

        verify(userDao,times(1)).create(any(User.class));
    }

    @Test
    void add_throwEmailAlreadyExistsException_ifSignUpDtoContainsDuplicateEmail() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException {
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adriel15rosario@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        when(userDao.emailExists(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.add(signUpDto));
    }

    @Test
    void add_throwUsernameAlreadyExistsException_ifSignUpDtoContainsDuplicateUsername() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException {
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adriel15rosario@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        when(userDao.emailExists(anyString())).thenReturn(false);
        when(userDao.usernameExists(anyString())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.add(signUpDto));
    }

    @Test
    void add_receiveSignUpDto_throwAuthorityNotFoundException_ifAuthorityIsNotValid() throws AuthorityNotFoundException {
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adriel15rosario@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        when(userDao.usernameExists(anyString())).thenReturn(false);
        when(userDao.emailExists(anyString())).thenReturn(false);
        when(authorityService.getByType(any())).thenThrow(new AuthorityNotFoundException("not found"));

        assertThrows(AuthorityNotFoundException.class,() -> userService.add(signUpDto));
    }

    @Test
    void add_receiveSignUpDto_throwProviderNotFoundException_ifProviderIsNotValid() throws ProviderNotFoundException, AuthorityNotFoundException {
        SignUpDto signUpDto = new SignUpDto(
                "adrielRosario15",
                "adriel15rosario@gmail.com",
                "Adriel Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        AuthorityDto authorityDto = new AuthorityDto(1,Authorities.READER);

        when(userDao.usernameExists(anyString())).thenReturn(false);
        when(userDao.emailExists(anyString())).thenReturn(false);
        when(authorityService.getByType(any())).thenReturn(authorityDto);
        when(providerService.getByType(any())).thenThrow(new ProviderNotFoundException("not found"));

        assertThrows(ProviderNotFoundException.class,() -> userService.add(signUpDto));
    }

    @Test
    void usernameExists_returnTrue_ifExists(){
        String validUsername = "adrielRosario";

        when(userDao.usernameExists(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            return validUsername.equals(username);
        });

        assertTrue(userService.usernameExists(validUsername));
    }

    @Test
    void usernameExists_returnFalse_ifNotExists(){
        String validUsername = "adrielRosario";

        when(userDao.usernameExists(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            return validUsername.equals(username);
        });

        assertFalse(userService.usernameExists("unknown"));
    }
}
