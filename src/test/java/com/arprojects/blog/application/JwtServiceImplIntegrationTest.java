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
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class JwtServiceImplIntegrationTest {

    @MockitoBean
    GoogleAuthService googleAuthService;

    @Mock
    Authentication authentication;

    @MockitoSpyBean
    private UserService userService;

    @MockitoSpyBean
    private UserDao userDao;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EntityManager entityManager;

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
        provider.setProvider(Providers.GOOGLE);
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
        user.setProviderUniqueId("1234");
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
    void generateJwtWithAuthentication_shouldReturn_jwtDtoIfUserIsValid(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_READER"));

        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn((Collection)authorities);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userDetails.getUserId()).thenReturn(1L);
        when(userDetails.getProfileName()).thenReturn("Test User");

        JwtDto jwtDto = jwtService.generateJwt(authentication);

        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals("testUser",decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_READER"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));
        assertNotNull(decodedJwt.getClaimAsString("profileName"));
    }

    @Test
    @Transactional
    void generateJwtWithGoogleLoginDto_shouldThrowGoogleLoginFailException_ifAccessTokenInvalid(){
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("invalidAccessToken");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.empty());

        assertThrows(GoogleLoginFailedException.class,() -> jwtService.generateJwt(googleLoginDto));
    }

    @Test
    @Transactional
    void generateJwtWithGoogleLoginDto_shouldThrowUserNotFoundException_ifNoUserWithProviderUIDExists() throws UserNotFoundException {
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("12345","adriel","adrielTest@gmail.com");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));

        when(userService.providerUIDExists(anyString())).thenReturn(true);

        assertThrows(UserNotFoundException.class,() -> jwtService.generateJwt(googleLoginDto));
    }

    @Test
    @Transactional
    void generateJwtWithGoogleLoginDto_shouldThrowEmailAlreadyExistsException_ifAnyUserAlreadyContainTheEmail(){
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("12345","adriel","adrielTest@gmail.com");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));

        when(userService.providerUIDExists(anyString())).thenReturn(false);

        assertThrows(EmailAlreadyExistsException.class, () -> jwtService.generateJwt(googleLoginDto));
    }

    @Test
    @Transactional
    void generateJwtWithGoogleLoginDto_returnJwtDto_whenAccessTokenIsValidAndUserExists() throws UserNotFoundException, ProviderNotFoundException, AuthorityNotFoundException, EmailAlreadyExistsException, GoogleLoginFailedException{
        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("1234","adriel","adrielTest@gmail.com");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));

        JwtDto jwtDto = jwtService.generateJwt(googleLoginDto);

        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals("adrielTest@gmail.com",decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_ADMIN"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));
        assertNotNull(decodedJwt.getClaimAsString("profileName"));
    }

    @Test
    @Transactional
    void generateJwtWithGoogleLoginDto_returnJwtDtoAndCreateNewUser_whenAccessTokenIsValidAndUserDoesNotExists() throws ProviderNotFoundException, AuthorityNotFoundException, UserNotFoundException, EmailAlreadyExistsException, GoogleLoginFailedException{
        //create and persist Authority
        Authority authority = new Authority();
        authority.setAuthority(Authorities.READER);
        entityManager.persist(authority);

        GoogleLoginDto googleLoginDto = new GoogleLoginDto("validAccessToken");
        GoogleInfoDto googleInfoDto = new GoogleInfoDto("12345","adriel","newUser@gmail.com");

        when(googleAuthService.authenticate(anyString())).thenReturn(Optional.of(googleInfoDto));

        JwtDto jwtDto = jwtService.generateJwt(googleLoginDto);

        Jwt decodedJwt = jwtDecoder.decode(jwtDto.token());

        assertEquals("newUser@gmail.com",decodedJwt.getSubject());
        assertTrue(decodedJwt.getClaimAsString("authorities").contains("ROLE_READER"));
        assertNotNull(decodedJwt.getClaimAsString("userId"));
        assertNotNull(decodedJwt.getClaimAsString("profileName"));

        verify(userDao,times(1)).create(any(User.class));
    }
}
