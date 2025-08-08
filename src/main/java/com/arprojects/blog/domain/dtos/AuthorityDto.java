package com.arprojects.blog.domain.dtos;

import com.arprojects.blog.domain.enums.Authorities;

public record AuthorityDto(int id, Authorities authorityType) {
}
