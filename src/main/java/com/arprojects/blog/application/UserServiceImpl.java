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
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDao userDao;
    private final AuthorityService authorityService;
    private final ProviderService providerService;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    @Autowired
    public UserServiceImpl(
            UserDao userDao,
            AuthorityService authorityService,
            ProviderService providerService,
            PasswordEncoder passwordEncoder,
            CacheManager cacheManager
    ){
        this.userDao = userDao;
        this.authorityService = authorityService;
        this.providerService = providerService;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(value = "emailExists", key = "#email", unless = "#result == false")
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Override
    @Cacheable(value = "providerUIDExists", key = "#providerUID", unless = "#result == false")
    public boolean existsByProviderUID(String providerUID) {
        return userDao.existsByProviderUID(providerUID);
    }

    @Override
    @Cacheable(value = "usersByProviderUID", key = "#providerUID")
    public UserDto getByProviderUID(String providerUID) throws UserNotFoundException {

        User user = userDao.getByProviderUID(providerUID)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapFromUserToUserDto.apply(user);
    }

    @Override
    @Transactional
    public UserDto addGoogleUser(AddGoogleUserDto addGoogleUserDto) throws AuthorityNotFoundException, ProviderNotFoundException {

        //use cache
        AuthorityDto authorityDto = authorityService.getByType(Authorities.READER);

        //user cache
        ProviderDto providerDto = providerService.getByType(Providers.GOOGLE);

        Profile profile = new Profile();
        profile.setProfileName(addGoogleUserDto.profileName());

        User user = new User();
        user.setEmail(addGoogleUserDto.email());
        user.setProviderUniqueId(addGoogleUserDto.providerUID());
        user.setEnabled(true);
        user.setProfile(profile);
        user.setAuthorities(Set.of(mapFromAuthorityDtoToAuthority.apply(authorityDto)));
        user.setProvider(mapFromProviderDtoToProvider.apply(providerDto));

        userDao.save(user);

        return mapFromUserToUserDto.apply(user);
    }

    @Override
    @Transactional
    public void add(SignUpDto signUpDto) throws EmailAlreadyExistsException, UsernameAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException {

        //verify email
        if(userDao.existsByEmail(signUpDto.email()))
            throw new EmailAlreadyExistsException("Email already in use");

        //verify username
        if(userDao.existsByUsername(signUpDto.username()))
            throw new UsernameAlreadyExistsException("Username already in use");

        //map from signUpDto to User
        User user = mapFromSignUpDtoToUser(signUpDto);

        //persist user
        userDao.save(user);
    }

    @Override
    @Cacheable(value = "usernameExists", key = "#username", unless = "#result == false")
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    @Override
    @Cacheable(value = "resetPasswordCode", key = "#email")
    public String generateResetPasswordCode(String email) throws EmailNotFoundException {
        if(!userDao.existsByEmail(email))
            throw new EmailNotFoundException("Email "+email+" is not valid.");

        String resetCode = UUID.randomUUID().toString();

        log.info("Email: {} reset code {}", email, resetCode);

        return resetCode;
    }

    @Override
    public void updatePassword(String password, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        //boolean reset = jwt.getClaim("reset");
        assert jwt != null;
        String email = jwt.getSubject();

        //if reset null throw and exception
        userDao.updatePasswordByEmail(new UpdateUserPasswordDto(email,password));

        Cache cache = cacheManager.getCache("resetPasswordCode");
        if (cache != null) cache.evict(email);
    }

    private User mapFromSignUpDtoToUser(SignUpDto signUpDto) throws AuthorityNotFoundException, ProviderNotFoundException {
        User user = new User();
        user.setUsername(signUpDto.username());
        user.setEmail(signUpDto.email());
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(signUpDto.password()));

        Profile profile = new Profile();
        profile.setBirthDate(signUpDto.birthDate());
        profile.setProfileName(signUpDto.profileName());
        user.setProfile(profile);

        AuthorityDto authorityDto = authorityService.getByType(Authorities.READER);
        ProviderDto providerDto = providerService.getByType(Providers.BASIC);

        user.setProvider(mapFromProviderDtoToProvider.apply(providerDto));
        user.setAuthorities(Set.of(mapFromAuthorityDtoToAuthority.apply(authorityDto)));

        return user;
    }

    Function<User,UserDto> mapFromUserToUserDto = (user -> {
        Set<Authorities> authorities = user.getAuthorities().stream().map(Authority::getAuthority).collect(Collectors.toSet());
        ProfileDto profileDto = new ProfileDto(user.getProfile().getId(), user.getProfile().getProfileName(), user.getProfile().getBirthDate());
        return new UserDto(user.getId(), user.getEmail(), user.getProvider().getProvider(),authorities,profileDto);
    });

    Function<AuthorityDto,Authority> mapFromAuthorityDtoToAuthority = (authorityDto -> {
        Authority authority = new Authority(authorityDto.authorityType());
        authority.setId(authorityDto.id());
        return authority;
    });

    Function<ProviderDto,Provider> mapFromProviderDtoToProvider = (providerDto -> {
        Provider provider = new Provider(providerDto.provider());
        provider.setId(providerDto.id());
        return provider;
    });

}
