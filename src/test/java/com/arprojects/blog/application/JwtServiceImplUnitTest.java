package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.*;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplUnitTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private GoogleAuthService googleAuthService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Test
    void generateJwtWithAuthentication_shouldReturn_jwtDtoIfUserIsValid(){

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_READER"));

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn((Collection)authorities);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getUserId()).thenReturn(1L);
        when(userDetails.getProfileName()).thenReturn("Test User");

        Jwt jwt = mock(Jwt.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("mocked-token");

        JwtDto result = jwtService.generateJwt(authentication);

        assertNotNull(result);
        assertEquals("mocked-token", result.token());

    }

    @Test
    void generateJwtWithGoogleLoginDto_shouldThrowGoogleLoginFailException_ifAccessTokenInvalid(){
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalidAccessToken");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.empty());

        assertThrows(GoogleLoginFailedException.class,() -> jwtService.generateJwt(googleLoginDto));
    }

    @Test
    void generateJwtWithGoogleLoginDto_shouldThrowUserNotFoundException_ifNoUserWithProviderUIDExists() throws UserNotFoundException {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("1234","Adriel","adriel15rosario@gmail.com");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));
        when(userService.providerUIDExists(anyString())).thenReturn(true);
        when(userService.getByProviderUID(anyString())).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class ,() -> jwtService.generateJwt(googleLoginDto));

    }

    @Test
    void generateJwtWithGoogleLoginDto_shouldThrowEmailAlreadyExistsException_ifAnyUserAlreadyContainTheEmail(){
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("1234","Adriel","adriel15rosario@gmail.com");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));
        when(userService.providerUIDExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,() -> jwtService.generateJwt(googleLoginDto));
    }

    @Test
    void generateJwtWithGoogleLoginDto_returnJwtDto_whenAccessTokenIsValidAndUserExists() throws UserNotFoundException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException, GoogleLoginFailedException {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("1234","Adriel","adriel15rosario@gmail.com");
        ProfileDto profileDto = new ProfileDto(1,"Adriel Rosario",LocalDate.now());
        UserDto userDto = new UserDto(1,"adriel@gmail.com",Providers.GOOGLE,Set.of(Authorities.READER),profileDto);

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));
        when(userService.providerUIDExists(anyString())).thenReturn(true);
        when(userService.getByProviderUID(anyString())).thenReturn(userDto);

        Jwt jwt = mock(Jwt.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("mocked-token");

        JwtDto jwtDto = jwtService.generateJwt(googleLoginDto);

        assertNotNull(jwtDto);
        assertEquals("mocked-token", jwtDto.token());
    }

    @Test
    void generateJwtWithGoogleLoginDto_returnJwtDtoAndCreateNewUser_whenAccessTokenIsValidAndUserDoesNotExists() throws ProviderNotFoundException, AuthorityNotFoundException, UserNotFoundException, EmailAlreadyExistsException, GoogleLoginFailedException {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("1234","Adriel","adriel15rosario@gmail.com");
        ProfileDto profileDto = new ProfileDto(1,"Adriel Rosario",LocalDate.now());
        UserDto userDto = new UserDto(1,"adriel@gmail.com",Providers.GOOGLE,Set.of(Authorities.READER),profileDto);

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));
        when(userService.providerUIDExists(anyString())).thenReturn(false);
        when(userService.emailExists(anyString())).thenReturn(false);
        when(userService.addGoogleUser(any(AddGoogleUserDto.class))).thenReturn(userDto);

        Jwt jwt = mock(Jwt.class);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("mocked-token");

        JwtDto jwtDto = jwtService.generateJwt(googleLoginDto);

        assertNotNull(jwtDto);
        assertEquals("mocked-token", jwtDto.token());
    }

}
