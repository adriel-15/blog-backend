package com.arprojects.blog.domain.converters;

import com.arprojects.blog.domain.enums.Authorities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorityConverterTest {
    private AuthorityConverter converter;

    @BeforeEach
    void setUp() {
        converter = new AuthorityConverter();
    }

    @Test
    void testConvertToDatabaseColumn_shouldReturnCorrectLabel() {
        assertEquals("ADMIN", converter.convertToDatabaseColumn(Authorities.ADMIN));
        assertEquals("WRITER", converter.convertToDatabaseColumn(Authorities.WRITER));
        assertEquals("READER", converter.convertToDatabaseColumn(Authorities.READER));
    }

    @Test
    void testConvertToDatabaseColumn_withNull_shouldReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute_shouldReturnCorrectEnum() {
        assertEquals(Authorities.ADMIN, converter.convertToEntityAttribute("ADMIN"));
        assertEquals(Authorities.WRITER, converter.convertToEntityAttribute("WRITER"));
        assertEquals(Authorities.READER, converter.convertToEntityAttribute("READER"));
    }

    @Test
    void testConvertToEntityAttribute_withNull_shouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testConvertToEntityAttribute_withInvalidLabel_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                converter.convertToEntityAttribute("UNKNOWN_ROLE"));

        assertEquals("Unknown authority: UNKNOWN_ROLE", exception.getMessage());
    }
}
