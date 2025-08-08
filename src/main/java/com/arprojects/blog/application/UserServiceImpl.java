package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.*;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.Profile;
import com.arprojects.blog.domain.entities.Provider;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.enums.Providers;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.domain.exceptions.ProviderNotFoundException;
import com.arprojects.blog.domain.exceptions.UserNotFoundException;
import com.arprojects.blog.ports.inbound.service_contracts.AuthorityService;
import com.arprojects.blog.ports.inbound.service_contracts.ProviderService;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final AuthorityService authorityService;
    private final ProviderService providerService;

    @Autowired
    public UserServiceImpl(UserDao userDao, AuthorityService authorityService,ProviderService providerService){
        this.userDao = userDao;
        this.authorityService = authorityService;
        this.providerService = providerService;
    }

    @Override
    @Cacheable(value = "emailExists", key = "#email")
    public boolean emailExists(String email) {
        return userDao.emailExists(email);
    }

    @Override
    @Cacheable(value = "providerUIDExists", key = "#providerUID")
    public boolean providerUIDExists(String providerUID) {
        return userDao.providerUIDExists(providerUID);
    }

    @Override
    @Cacheable(value = "usersByProviderUID", key = "#providerUID")
    public UserDto getUserByProviderUID(String providerUID) throws UserNotFoundException {

        User user = userDao.getUserByProviderUID(providerUID)
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

        userDao.create(user);

        return mapFromUserToUserDto.apply(user);
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
