package com.arprojects.blog.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Entity
@Table(name = "USERS")
public class User extends Auditable<String>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "USERNAME", unique = true,length = 100)
    @Size(max = 100)
    private String username;

    @Column(name = "PASSWORD", length = 300)
    @Size(max = 300)
    private String password;

    @Column(name = "EMAIL", unique = true, length = 320, nullable = false)
    @NotNull
    @Email
    @Size(max = 320)
    private String email;

    @Column(name = "ENABLED", nullable = false)
    @NotNull
    private boolean enabled;

    @Column(name = "PROVIDER_UID", unique = true, length = 300)
    @Size(max = 300)
    private String providerUniqueId;

    @ManyToOne
    @JoinColumn(name = "PROVIDER_ID", nullable = false)
    @NotNull
    private Provider provider;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USERS_AUTHORITIES", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "AUTHORITY_ID"))
    private Set<Authority> authorities;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PROFILE_ID", nullable = false, unique = true)
    @NotNull
    private Profile profile;

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProviderUniqueId() {
        return providerUniqueId;
    }

    public void setProviderUniqueId(String providerUniqueId) {
        this.providerUniqueId = providerUniqueId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
