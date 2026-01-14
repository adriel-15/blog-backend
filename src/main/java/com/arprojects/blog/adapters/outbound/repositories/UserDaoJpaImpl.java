package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.dtos.UpdateUserPasswordDto;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserDaoJpaImpl implements UserDao {

    private final EntityManager entityManager;

    @Autowired
    public UserDaoJpaImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<User> getByUsername(String username) {

        String query = "select u from User u"
                + " join fetch u.authorities" +
                " where u.username = :username";

        try {
            User user = entityManager
                    .createQuery(query, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.of(user);
        } catch (Exception _) {
            return Optional.empty();
        }

    }

    @Override
    public Optional<User> getByProviderUID(String providerUID) {

        String query = "select u from User u" +
                " join fetch u.authorities" +
                " where u.providerUniqueId = :providerUID";

        try {
            User user = entityManager
                    .createQuery(query, User.class)
                    .setParameter("providerUID", providerUID)
                    .getSingleResult();

            return Optional.of(user);
        } catch (Exception _) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void save(User user) {
        entityManager.persist(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        String query = "select count(u) > 0 from User u where u.email=:email";

        return entityManager.createQuery(query,Boolean.class)
                .setParameter("email",email)
                .getSingleResult();
    }

    @Override
    public boolean existsByProviderUID(String providerUID) {
        String query = "select count(u) > 0 from User u where u.providerUniqueId=:providerUniqueId";

        return entityManager.createQuery(query,Boolean.class)
                .setParameter("providerUniqueId",providerUID)
                .getSingleResult();
    }

    @Override
    public boolean existsByUsername(String username) {
        String query = "select count(u) > 0 from User u where u.username=:username";

        return entityManager.createQuery(query, Boolean.class)
                .setParameter("username",username)
                .getSingleResult();
    }

    @Override
    @Transactional
    public void updatePasswordByEmail(UpdateUserPasswordDto updateUserPasswordDto) {
        String query = "update User u set u.password=:password where u.email=:email";

        entityManager.createQuery(query)
                .setParameter("password",updateUserPasswordDto.password())
                .setParameter("email",updateUserPasswordDto.email())
                .executeUpdate();
    }

    @Override
    @Transactional
    public void deleteAll() {
        entityManager.createQuery("delete from User").executeUpdate();
    }

}
