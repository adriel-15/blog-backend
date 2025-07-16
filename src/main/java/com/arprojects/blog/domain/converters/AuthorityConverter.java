package com.arprojects.blog.domain.converters;

import com.arprojects.blog.domain.enums.Authorities;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuthorityConverter implements AttributeConverter<Authorities,String> {
    @Override
    public String convertToDatabaseColumn(Authorities authorities) {
        return authorities != null ? authorities.getLabel() : null;
    }

    @Override
    public Authorities convertToEntityAttribute(String dbValue) {
        return dbValue != null ? Authorities.fromLabel(dbValue) : null;
    }
}
