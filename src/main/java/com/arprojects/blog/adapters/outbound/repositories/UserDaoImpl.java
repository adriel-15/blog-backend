package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final EntityManager entityManager;

    @Autowired
    public UserDaoImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {

        String query = "select u from User u"
                +" join fetch u.authorities"+
                " where u.username = :username";

        try {
            User user = entityManager.createQuery(query,User.class)
                    .setParameter("username",username)
                    .getSingleResult();

            return Optional.of(user);
        }catch (Exception e){
            return Optional.empty();
        }

    }
}
