package com.arprojects.blog.domain.converters;

import com.arprojects.blog.domain.enums.Providers;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProviderConverter implements AttributeConverter<Providers,String> {
    @Override
    public String convertToDatabaseColumn(Providers providers) {
        return providers != null ? providers.getLabel() : null;
    }

    @Override
    public Providers convertToEntityAttribute(String dbValue) {
        return dbValue != null ? Providers.fromLabel(dbValue) : null;
    }
}
