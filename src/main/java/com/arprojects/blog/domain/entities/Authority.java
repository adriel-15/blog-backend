package com.arprojects.blog.domain.entities;

import com.arprojects.blog.domain.enums.Authorities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "AUTHORITIES")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "AUTHORITY")
    private Authorities authority;

    @ManyToMany(mappedBy = "authorities",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;

    public Authority() {
    }

    public Authority(Authorities authority){
        this.authority = authority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Authorities getAuthority() {
        return authority;
    }

    public void setAuthority(Authorities authority) {
        this.authority = authority;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
