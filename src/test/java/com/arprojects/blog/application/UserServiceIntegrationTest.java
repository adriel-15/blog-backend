package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.AddGoogleUserDto;
import com.arprojects.blog.domain.dtos.SignUpDto;
import com.arprojects.blog.domain.dtos.UserDto;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest extends BaseServiceIntegrationTest{

    @Test
    @DisplayName("existsByEmail(String email) - return true if a user already have that email.")
    void existsByEmail_returnTrue(){
        seeDefaultUser();

        //first call should hit the database
        boolean first = userService.existsByEmail("johnDoe@gmail.com");

        //second call should hit the cache
        boolean second = userService.existsByEmail("johnDoe@gmail.com");

        assertTrue(first);
        assertTrue(second);
        verify(userDao, times(1)).existsByEmail("johnDoe@gmail.com");

    }

    @Test
    @DisplayName("existsByEmail(String email) - return false if no user have that email.")
    void existsByEmail_returnFalse(){
        boolean exists = userService.existsByEmail("notexists@gmail.com");
        assertFalse(exists);
    }

    @Test
    @DisplayName("existsByProviderUID(String uid) - return true if a user already have that providerUID")
    void existsByProviderUID_returnTrue(){
        seeDefaultUser();

        //first call should hit the database
        boolean first = userService.existsByProviderUID("1234");

        //second call should hit the cache
        boolean second = userService.existsByProviderUID("1234");

        assertTrue(first);
        assertTrue(second);
        verify(userDao, times(1)).existsByProviderUID("1234");
    }

    @Test
    @DisplayName("existsByProviderUID(String uid) - return false if no user have that providerUID")
    void existsByProviderUID_returnFalse(){
        boolean exists =userService.existsByProviderUID("invalid-provider-uid");
        assertFalse(exists);
    }

    @Test
    @DisplayName("getByProviderUID(String providerUID) - return UserDto if user with providerUID exists.")
    void getByProviderUID_returnUserDto() throws UserNotFoundException {
        seeDefaultUser();

        //first call should hit the database
        userService.getByProviderUID("1234");

        //second call should hit the cache
        UserDto second = userService.getByProviderUID("1234");

        verify(userDao,times(1)).getByProviderUID("1234");

        assertEquals("johnDoe@gmail.com",second.email());
        assertEquals(Providers.BASIC,second.provider());
    }

    @Test
    @DisplayName("getByProviderUID(String providerUID) - throw UserNotFoundException")
    void getByProviderUID_throwUserNotFoundException() {
        assertThrows(UserNotFoundException.class,() -> userService.getByProviderUID("invalid-provider"));
    }

    @Test
    @DisplayName("existsByUsername(String username) - return true if a user already have the username.")
    void existsByUsername_returnTrue(){
        seeDefaultUser();

        //first call should hit database
        boolean first = userService.existsByUsername("johnDoe");

        //second call should hit cache
        boolean second = userService.existsByUsername("johnDoe");

        assertTrue(first);
        assertTrue(second);
        verify(userDao,times(1)).existsByUsername("johnDoe");
    }

    @Test
    @DisplayName("existsByUsername(String username) - return false if no user have the username.")
    void existsByUsername_returnFalse(){
        boolean exists = userService.existsByUsername("unknown-username");
        assertFalse(exists);
    }

    @Test
    @DisplayName("addGoogleUser(dto) - add new user and return userDto if successfully created.")
    void addGoogleUser_returnUserDto() throws ProviderNotFoundException, AuthorityNotFoundException {
        seedAuthorities();
        seedProviders();

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
    @DisplayName("add(dto) - add new user")
    void add_createUser() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException {
        seedAuthorities();
        seedProviders();

        SignUpDto signUpDto = new SignUpDto(
                "alex19",
                "alex19rosario@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        userService.add(signUpDto);

        verify(userDao,times(1)).save(any());
    }

    @Test
    @DisplayName("add(dto) - throw EmailAlreadyExistsException")
    void add_throwEmailAlreadyExistsException() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException {
        seeDefaultUser();

        SignUpDto signUpDto = new SignUpDto(
                "alex19",
                "johnDoe@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        assertThrows(EmailAlreadyExistsException.class, () -> userService.add(signUpDto));
    }

    @Test
    @DisplayName("add(dto) - throw UsernameAlreadyExistsException")
    void add_throwUsernameAlreadyExistsException() throws UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException {
        seeDefaultUser();

        SignUpDto signUpDto = new SignUpDto(
                "johnDoe",
                "adriel14@gmail.com",
                "Alex Rosario Sanchez",
                LocalDate.of(2000,9,15),
                "@AlexRosario1234"
        );

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.add(signUpDto));
    }

}
