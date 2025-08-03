package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.CustomUserDetails;
import com.arprojects.blog.domain.dtos.GoogleInfoDto;
import com.arprojects.blog.domain.dtos.GoogleLoginDto;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.GoogleLoginFailedException;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
    private UserDao userDao;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtEncoderParameters jwtEncoderParameters;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Authority> authorityQuery;

    @Mock
    private TypedQuery<Provider> providerQuery;

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Test
    void should_returnJwtDto_when_userDetailsValid(){
        //given
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_READER"));

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn((Collection)authorities);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getUserId()).thenReturn(1L);
        when(userDetails.getProfileName()).thenReturn("Test User");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        JwtDto result = jwtService.generateJwt(authentication);

        assertNotNull(result);
        assertEquals("mocked-token", result.token());
    }

    @Test
    void generateJwt_withExistingUser_returnsToken() throws GoogleLoginFailedException {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("access-token");

        GoogleInfoDto googleInfoDto = new GoogleInfoDto("sub123","Test User", "test@example.com");
        when(googleAuthService.authenticate("access-token")).thenReturn(Optional.of(googleInfoDto));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setProviderUniqueId("sub123");
        user.setUsername("test@example.com");

        Profile profile = new Profile();
        profile.setProfileName("Test User");
        user.setProfile(profile);

        user.setAuthorities(Set.of(new Authority(com.arprojects.blog.domain.enums.Authorities.READER)));

        when(userDao.getUserByProviderUID("sub123")).thenReturn(Optional.of(user));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("existing-user-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        JwtDto result = jwtService.generateJwt(googleLoginDto);

        assertNotNull(result);
        assertEquals("existing-user-token", result.token());
    }

    @Test
    void generateJwt_withNewUser_createsUserAndReturnsToken() throws GoogleLoginFailedException {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("access-token");

        GoogleInfoDto googleInfoDto = new GoogleInfoDto("sub123","New User", "newuser@example.com");
        when(googleAuthService.authenticate("access-token")).thenReturn(Optional.of(googleInfoDto));
        when(userDao.getUserByProviderUID("sub123")).thenReturn(Optional.empty());

        // simulate database assigning id and username
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setUsername("newuser@example.com");
            return null;
        }).when(userDao).create(any(User.class));

        when(entityManager.createQuery(
                "select a from Authority a where a.authorityType = :authorityType",
                Authority.class)).thenReturn(authorityQuery);
        when(authorityQuery.setParameter("authorityType",Authorities.READER)).thenReturn(authorityQuery);
        when(authorityQuery.getSingleResult()).thenReturn(new Authority(Authorities.READER));

        when(entityManager.createQuery(
                "select p from Provider p where p.providerType = :providerType",
                Provider.class)).thenReturn(providerQuery);
        when(providerQuery.setParameter("providerType", Providers.GOOGLE)).thenReturn(providerQuery);
        when(providerQuery.getSingleResult()).thenReturn(new Provider(Providers.GOOGLE));
        
        //simulate
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("new-user-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        JwtDto result = jwtService.generateJwt(googleLoginDto);

        assertNotNull(result);
        assertEquals("new-user-token", result.token());
        verify(userDao).create(any(User.class)); // ensure user was created
    }

    @Test
    void generateJwt_withInvalidGoogleToken_throwsException() {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalid-token");

        when(googleAuthService.authenticate("invalid-token")).thenReturn(Optional.empty());

        assertThrows(GoogleLoginFailedException.class, () -> {
            jwtService.generateJwt(googleLoginDto);
        });
    }
}
