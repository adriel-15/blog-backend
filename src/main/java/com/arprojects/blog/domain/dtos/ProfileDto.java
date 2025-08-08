package com.arprojects.blog.domain.dtos;

import java.time.LocalDate;

public record ProfileDto(long id, String profileName, LocalDate birthDate) {
}
