package com.arprojects.blog.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    @Test
    void test_profileEntity_GettersAndSetters(){

        //arrange
        Profile profile = new Profile();
        String expectedProfileName = "adriel15";
        LocalDate expectedBirthDate = LocalDate.of(2000,9,15);

        //act
        profile.setId(1);
        profile.setProfileName(expectedProfileName);
        profile.setBirthDate(expectedBirthDate);
        profile.setUser(new User());

        //assert
        assertEquals(1,profile.getId());
        assertEquals(expectedProfileName,profile.getProfileName());
        assertEquals(expectedBirthDate,profile.getBirthDate());
        assertNotNull(profile.getUser());

    }

}
