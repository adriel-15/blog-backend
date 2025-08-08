package com.arprojects.blog.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    @Test
    void test_profileEntity_GettersAndSetters(){

        //arrange
        Profile profile = new Profile();
        String expectedProfileName = "adriel15";
        LocalDate expectedBirthDate = LocalDate.of(2000,9,15);
        String createByUser = "adonis22";
        String updateByUser = "alex19";


        //act
        profile.setId(1);
        profile.setProfileName(expectedProfileName);
        profile.setBirthDate(expectedBirthDate);
        profile.setUser(new User());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        profile.setCreatedBy(createByUser);
        profile.setUpdatedBy(updateByUser);

        //assert
        assertEquals(1,profile.getId());
        assertEquals(expectedProfileName,profile.getProfileName());
        assertEquals(expectedBirthDate,profile.getBirthDate());
        assertNotNull(profile.getUser());
        assertNotNull(profile.getCreatedAt());
        assertNotNull(profile.getUpdatedAt());
        assertEquals(createByUser,profile.getCreatedBy());
        assertEquals(updateByUser, profile.getUpdatedBy());

    }


    @Test
    void test_profileEntity_Constructors(){
        Profile profile = new Profile("Adriel Rosario");

        assertEquals("Adriel Rosario", profile.getProfileName());
    }
}
