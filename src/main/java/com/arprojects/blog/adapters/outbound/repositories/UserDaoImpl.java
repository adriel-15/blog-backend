package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.exceptions.*;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final EntityManager entityManager;

    @Value("${duplicate-email-error}")
    private String duplicateEmailError;

    @Value("${duplicate-username-error}")
    private String duplicateUsernameError;

    @Value("${duplicate-provideruid-error}")
    private String duplicateProviderUIDError;

    @Autowired
    public UserDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {

        String query = "select u from User u"
                + " join fetch u.authorities" +
                " where u.username = :username";

        try {
            User user = entityManager
                    .createQuery(query, User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    @Override
    public Optional<User> getUserByProviderUID(String providerUID) {

        String query = "select u from User u" +
                " join fetch u.authorities" +
                " where u.providerUniqueId = :providerUID";

        try {
            User user = entityManager
                    .createQuery(query, User.class)
                    .setParameter("providerUID", providerUID)
                    .getSingleResult();

            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void create(User user) {
        try{
            entityManager.persist(user);
        }catch (ConstraintViolationException ex){
            String message = ex.getMessage();
            if(message.contains(duplicateEmailError)){
                throw new EmailAlreadyExistsException("Email already in use");
            }else if(message.contains(duplicateUsernameError)){
                throw new UsernameAlreadyExistsException("Username already taken");
            }else if(message.contains(duplicateProviderUIDError)){
                throw new ProviderUIDAlreadyExistsException("Provider unique id must be unique");
            }else{
                throw new DuplicateKeyException("A constraint was violated");
            }
        }catch (PersistenceException ex){
            throw new UserCreationException("Persistence error", ex);
        }catch (Exception ex){
            throw new UserCreationException("Unexpected error", ex);
        }
    }

    public void setDuplicateEmailError(String duplicateEmailError) {
        this.duplicateEmailError = duplicateEmailError;
    }

    public void setDuplicateUsernameError(String duplicateUsernameError) {
        this.duplicateUsernameError = duplicateUsernameError;
    }

    public void setDuplicateProviderUIDError(String duplicateProviderUIDError) {
        this.duplicateProviderUIDError = duplicateProviderUIDError;
    }

}
