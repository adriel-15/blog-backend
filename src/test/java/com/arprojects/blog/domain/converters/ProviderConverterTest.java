package com.arprojects.blog.domain.converters;
import com.arprojects.blog.domain.enums.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProviderConverterTest {
    private ProviderConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ProviderConverter();
    }

    @Test
    void testConvertToDatabaseColumn_shouldReturnCorrectLabel() {
        assertEquals("BASIC", converter.convertToDatabaseColumn(Providers.BASIC));
        assertEquals("GOOGLE", converter.convertToDatabaseColumn(Providers.GOOGLE));
    }

    @Test
    void testConvertToDatabaseColumn_withNull_shouldReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute_shouldReturnCorrectEnum() {
        assertEquals(Providers.BASIC, converter.convertToEntityAttribute("BASIC"));
        assertEquals(Providers.GOOGLE, converter.convertToEntityAttribute("GOOGLE"));
    }

    @Test
    void testConvertToEntityAttribute_withNull_shouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testConvertToEntityAttribute_withInvalidLabel_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                converter.convertToEntityAttribute("UNKNOWN PROVIDER"));

        assertEquals("Unknown provider: UNKNOWN PROVIDER", exception.getMessage());
    }

}
