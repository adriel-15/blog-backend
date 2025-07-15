package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.CustomUserDetails;
import com.arprojects.blog.domain.dtos.JwtDto;
import com.arprojects.blog.ports.inbound.service_contracts.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public JwtServiceImpl(JwtEncoder jwtEncoder){
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public JwtDto generateJwt(Authentication authentication) {
        Instant now = Instant.now();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("authorities", authorities)
                .build();

        return new JwtDto(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue());

    }
}
