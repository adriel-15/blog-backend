package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.AuthorityDto;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;
import com.arprojects.blog.ports.inbound.service_contracts.AuthorityService;
import com.arprojects.blog.ports.outbound.repository_contracts.AuthorityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityDao authorityDao;

    @Autowired
    public AuthorityServiceImpl(AuthorityDao authorityDao){
        this.authorityDao = authorityDao;
    }

    @Override
    @Cacheable(value = "authorityByType", key = "#authorityType")
    public AuthorityDto getByType(Authorities authorityType) throws AuthorityNotFoundException {

        Authority authority = authorityDao.getAuthorityByType(authorityType)
                .orElseThrow(() -> new AuthorityNotFoundException("Authority does not exists"));

        return mapFromAuthorityToAuthorityDto.apply(authority);
    }

    Function<Authority,AuthorityDto> mapFromAuthorityToAuthorityDto = (authority -> {
        return new AuthorityDto(authority.getId(),authority.getAuthority());
    });
}
