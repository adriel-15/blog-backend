package com.arprojects.blog.ports.inbound.service_contracts;

import com.arprojects.blog.domain.dtos.AuthorityDto;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.domain.exceptions.AuthorityNotFoundException;

public interface AuthorityService {

    AuthorityDto getByType(Authorities authorityType) throws AuthorityNotFoundException;

}
