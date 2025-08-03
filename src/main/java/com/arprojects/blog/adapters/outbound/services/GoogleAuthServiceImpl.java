package com.arprojects.blog.adapters.outbound.services;

import com.arprojects.blog.domain.dtos.GoogleInfoDto;
import com.arprojects.blog.ports.outbound.service_contracts.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final RestTemplate restTemplate;

    @Autowired
    public GoogleAuthServiceImpl(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<GoogleInfoDto> authenticate(String googleAccessToken) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(googleAccessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<GoogleInfoDto> response = this.restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    GoogleInfoDto.class
            );

            GoogleInfoDto googleInfoDto = response.getBody();

            if(googleInfoDto.email() == null || googleInfoDto.sub() == null || googleInfoDto.name() == null){
                return Optional.empty();
            }else{
                return Optional.of(googleInfoDto);
            }
        } catch (Exception e){
            return Optional.empty();
        }
    }
}
