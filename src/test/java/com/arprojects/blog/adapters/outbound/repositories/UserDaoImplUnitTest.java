package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.exceptions.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoImplUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> query;

    @InjectMocks
    private UserDaoImpl userDao;

    @BeforeEach
    void setUp() {

        userDao.setDuplicateEmailError("EMAIL_UNIQUE_KEY");
        userDao.setDuplicateUsernameError("USERNAME_UNIQUE_KEY");
        userDao.setDuplicateProviderUIDError("PROVIDER_UID_UNIQUE_KEY");
    }

    @Test
    void shouldReturnUser_whenUserExists() {
        // Arrange
        String username = "john";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setPassword("mockpassword");

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(expectedUser);

        // Act
        Optional<User> result = userDao.getUserByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        assertEquals(expectedUser.getPassword(),result.get().getPassword());
    }

    @Test
    void shouldReturnEmptyOptional_whenExceptionOccurs() {
        // Arrange
        String username = "unknown";

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new RuntimeException("User not found"));

        // Act
        Optional<User> result = userDao.getUserByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldReturnUser_whenProviderUIDExists() {
        // Arrange
        String providerUID = "google-12345";
        User expectedUser = new User();
        expectedUser.setProviderUniqueId(providerUID);

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("providerUID", providerUID)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(expectedUser);

        // Act
        Optional<User> result = userDao.getUserByProviderUID(providerUID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(providerUID, result.get().getProviderUniqueId());
    }

    @Test
    void shouldReturnEmptyOptional_whenProviderUIDNotFound() {
        // Arrange
        String providerUID = "nonexistent-uid";

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("providerUID", providerUID)).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new RuntimeException("Not found"));

        // Act
        Optional<User> result = userDao.getUserByProviderUID(providerUID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPersistUserSuccessfully() {
        // Arrange
        User user = new User();
        // No mocking needed, assume persist doesn't throw anything

        // Act & Assert
        assertDoesNotThrow(() -> userDao.create(user));
        verify(entityManager, times(1)).persist(user);
    }

    @Test
    void shouldThrowEmailAlreadyExists_whenEmailConstraintViolated() {
        // Arrange
        User user = new User();
        SQLException sqlException = mock(SQLException.class);
        ConstraintViolationException ex = new ConstraintViolationException("EMAIL_UNIQUE_KEY",sqlException,null);

        doThrow(ex).when(entityManager).persist(user);

        // Act & Assert
        EmailAlreadyExistsException error = assertThrows(EmailAlreadyExistsException.class, () -> userDao.create(user));
        assertEquals("Email already in use", error.getMessage());
    }

    @Test
    void shouldThrowUsernameAlreadyExists_whenUsernameConstraintViolated() {
        User user = new User();
        SQLException sqlException = mock(SQLException.class);
        ConstraintViolationException ex = new ConstraintViolationException("USERNAME_UNIQUE_KEY",sqlException,null);

        doThrow(ex).when(entityManager).persist(user);

        UsernameAlreadyExistsException error = assertThrows(UsernameAlreadyExistsException.class, () -> userDao.create(user));
        assertEquals("Username already taken", error.getMessage());
    }


    @Test
    void shouldThrowProviderUIDAlreadyExists_whenProviderUIDConstraintViolated() {
        User user = new User();
        SQLException sqlException = mock(SQLException.class);
        ConstraintViolationException ex = new ConstraintViolationException("PROVIDER_UID_UNIQUE_KEY",sqlException,null);

        doThrow(ex).when(entityManager).persist(user);

        ProviderUIDAlreadyExistsException error = assertThrows(ProviderUIDAlreadyExistsException.class, () -> userDao.create(user));
        assertEquals("Provider unique id must be unique", error.getMessage());
    }


    @Test
    void shouldThrowDuplicateKeyException_whenOtherUniqueConstraintViolated() {
        User user = new User();
        SQLException sqlException = mock(SQLException.class);
        ConstraintViolationException ex = new ConstraintViolationException("duplicate key",sqlException,null);

        doThrow(ex).when(entityManager).persist(user);

        DuplicateKeyException error = assertThrows(DuplicateKeyException.class, () -> userDao.create(user));
        assertEquals("A constraint was violated", error.getMessage());
    }

    @Test
    void shouldThrowUserCreationException_whenPersistenceExceptionOccurs() {
        User user = new User();
        PersistenceException ex = new PersistenceException();

        doThrow(ex).when(entityManager).persist(user);

        UserCreationException error = assertThrows(UserCreationException.class, () -> userDao.create(user));
        assertEquals("Persistence error", error.getMessage());
    }

    @Test
    void shouldThrowUserCreationException_whenUnexpectedErrorOccurs() {
        User user = new User();
        RuntimeException ex = new RuntimeException();

        doThrow(ex).when(entityManager).persist(user);

        UserCreationException error = assertThrows(UserCreationException.class, () -> userDao.create(user));
        assertEquals("Unexpected error", error.getMessage());
    }

}
