package com.arprojects.blog.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProvidersTest {

    @Test
    void getLabel_shouldReturn_correctLabel(){
        assertEquals("BASIC",Providers.BASIC.getLabel());
        assertEquals("GOOGLE",Providers.GOOGLE.getLabel());
    }

    @Test
    void fromLabel_shouldReturn_correctEnum_ignoringCase(){
        assertEquals(Providers.BASIC, Providers.fromLabel("basic"));
        assertEquals(Providers.GOOGLE, Providers.fromLabel("GOOGLE"));
    }

    @Test
    void fromLabel_shouldReturn_exception_ifLabelInvalid(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            Providers.fromLabel("Invalid Provider");
        });

        assertEquals("Unknown provider: Invalid Provider",ex.getMessage());
    }
}
