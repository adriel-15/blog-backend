package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.*;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import com.arprojects.blog.ports.inbound.service_contracts.UserService;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder jwtEncoder;
    private final GoogleAuthService googleAuthService;
    private final UserService userService;
    private final CacheManager cacheManager;

    @Autowired
    public JwtServiceImpl(
            JwtEncoder jwtEncoder,
            GoogleAuthService googleAuthService,
            UserService userService,
            CacheManager cacheManager
    ){
        this.jwtEncoder = jwtEncoder;
        this.googleAuthService = googleAuthService;
        this.userService = userService;
        this.cacheManager = cacheManager;
    }

    @Override
    public JwtDto generateJwt(Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return buildJwtDto(userDetails);
    }

    @Override
    public JwtDto generateJwt(GoogleLoginDto googleLoginDto) throws GoogleLoginFailedException, UserNotFoundException, EmailAlreadyExistsException, ProviderNotFoundException, AuthorityNotFoundException {

        //retrieve google user info if google access token is valid
        GoogleInfoDto googleInfoDto = this.googleAuthService.authenticate(googleLoginDto.googleAccessToken())
                .orElseThrow(() -> new GoogleLoginFailedException("Failed to retrieve google user info"));

        if(userService.existsByProviderUID(googleInfoDto.sub())){
            //cache
            UserDto userDto = userService.getByProviderUID(googleInfoDto.sub());
            return buildJwtDto(userDto);
        }else{
            //cache
            if(userService.existsByEmail(googleInfoDto.email()))
                throw new EmailAlreadyExistsException("Email already in use");

            AddGoogleUserDto user = new AddGoogleUserDto(
                    googleInfoDto.email(),
                    googleInfoDto.sub(),
                    true,
                    googleInfoDto.name()
            );

            return buildJwtDto(userService.addGoogleUser(user));
        }
    }

    @Override
    public JwtDto generateJwt(VerifyResetPasswordCodeDto verifyResetPasswordCodeDto) {

        Cache cache = cacheManager.getCache("resetPasswordCode");

        //if cache is null throw invalid code exception

        String storedCode = cache.get(verifyResetPasswordCodeDto.email(),String.class);

        //if storedCode is null throw invalid code exception

        boolean isValid = storedCode.equals(verifyResetPasswordCodeDto.code());

        if(!isValid){
            //throw invalid code exception
            return null; // MUST THROW AN EXCEPTION!!!!
        }else{
            return buildJwtDto(verifyResetPasswordCodeDto);
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

    private JwtDto buildJwtDto(UserDto userDto){
        Instant now = Instant.now();

        String authorities = userDto.authorities().stream()
                .map(authority ->"ROLE_"+authority.getLabel())
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(userDto.email())
                .claim("userId", userDto.id())
                .claim("profileName",userDto.profile().profileName())
                .claim("authorities", authorities)
                .build();

        String tokenValue = this.jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        return new JwtDto(tokenValue);
    }

    private JwtDto buildJwtDto(VerifyResetPasswordCodeDto verifyResetPasswordCodeDto){
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(3, ChronoUnit.MINUTES)) // shorter!
                .subject(verifyResetPasswordCodeDto.email())
                .claim("reset", true)
                .build();

        String tokenValue = this.jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        return new JwtDto(tokenValue);
    }

}
