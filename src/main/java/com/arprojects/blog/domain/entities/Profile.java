package com.arprojects.blog.domain.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "PROFILES")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "PROFILE_NAME")
    private String profileName;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL)
    private User user;

    public Profile() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
