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
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder jwtEncoder;
    private final GoogleAuthService googleAuthService;
    private final UserDao userDao;
    private final EntityManager entityManager;

    @Autowired
    public JwtServiceImpl(JwtEncoder jwtEncoder,GoogleAuthService googleAuthService,UserDao userDao,EntityManager entityManager){
        this.jwtEncoder = jwtEncoder;
        this.googleAuthService = googleAuthService;
        this.userDao = userDao;
        this.entityManager = entityManager;
    }

    @Override
    public JwtDto generateJwt(Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return buildJwtDto(userDetails);
    }

    @Override
    public JwtDto generateJwt(GoogleLoginDto googleLoginDto) throws GoogleLoginFailedException {

        //retrieve google user info if google access token is valid
        GoogleInfoDto googleInfoDto = this.googleAuthService.authenticate(googleLoginDto.googleAccessToken())
                .orElseThrow(() -> new GoogleLoginFailedException("Failed to retrieve google user info"));

        //get user by providerUID or sub
        Optional<User> userOptional = this.userDao.getUserByProviderUID(googleInfoDto.sub());

        if(userOptional.isPresent()){

            return buildJwtDto(userOptional.get());

        }else{

            User user = new User();
            user.setEmail(googleInfoDto.email());
            user.setProviderUniqueId(googleInfoDto.sub());
            user.setEnabled(true);

            Profile profile = new Profile();
            profile.setProfileName(googleInfoDto.name());

//            Set<Authority> authorities = Set.of(new Authority(Authorities.READER));
//
//            Provider provider = new Provider();
//            provider.setProvider(Providers.GOOGLE);

            Authority authority = entityManager.createQuery(
                    "select a from Authority a where a.authorityType = :authorityType",
                    Authority.class
            ).setParameter("authorityType",Authorities.READER).getSingleResult();

            Provider provider = entityManager.createQuery(
                    "select p from Provider p where p.providerType = :providerType",
                    Provider.class
            ).setParameter("providerType",Providers.GOOGLE).getSingleResult();

            user.setProfile(profile);
            user.setAuthorities(Set.of(authority));
            user.setProvider(provider);

            userDao.create(user);

            return buildJwtDto(user);
        }
    }

    private JwtDto buildJwtDto(CustomUserDetails userDetails){
        Instant now = Instant.now();

        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("profileName",userDetails.getProfileName())
                .claim("authorities", authorities)
                .build();

        String tokenValue = this.jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        return new JwtDto(tokenValue);
    }

    private JwtDto buildJwtDto(User user){
        Instant now = Instant.now();

        String authorities = user.getAuthorities().stream()
                .map(authority ->"ROLE_"+authority.getAuthority().getLabel())
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("profileName",user.getProfile().getProfileName())
                .claim("authorities", authorities)
                .build();

        String tokenValue = this.jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        return new JwtDto(tokenValue);
    }

}
