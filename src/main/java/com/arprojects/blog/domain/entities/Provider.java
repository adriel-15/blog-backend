package com.arprojects.blog.domain.entities;

import com.arprojects.blog.domain.enums.Providers;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "PROVIDERS")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "PROVIDER")
    private Providers providerType;

    @OneToMany(mappedBy = "provider")
    private List<User> users;

    public Provider() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Providers getProvider() {
        return providerType;
    }

    public void setProvider(Providers providerType) {
        this.providerType = providerType;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
