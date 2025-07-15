package com.arprojects.blog.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class AuthoritiesTest {

    @Test
    void getLabel_shouldReturn_correctLabel(){
        assertEquals("ADMIN",Authorities.ADMIN.getLabel());
        assertEquals("WRITER",Authorities.WRITER.getLabel());
        assertEquals("READER",Authorities.READER.getLabel());
    }

    @Test
    void fromLabel_shouldReturn_correctEnum_ignoringCase(){
        assertEquals(Authorities.ADMIN, Authorities.fromLabel("admin"));
        assertEquals(Authorities.WRITER, Authorities.fromLabel("Writer"));
        assertEquals(Authorities.READER, Authorities.fromLabel("READER"));
    }

    @Test
    void fromLabel_shouldReturn_exception_ifLabelInvalid(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            Authorities.fromLabel("Invalid Authority");
        });

        assertEquals("Unknown authority: Invalid Authority",ex.getMessage());
    }


}
