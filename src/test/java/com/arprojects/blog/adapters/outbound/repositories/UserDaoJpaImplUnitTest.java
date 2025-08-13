package com.arprojects.blog.adapters.outbound.repositories;

import com.arprojects.blog.domain.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoJpaImplUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> userTypedQuery;

    @Mock
    private TypedQuery<Boolean> booleanTypedQuery;

    @InjectMocks
    private UserDaoJpaImpl userDao;

    @Test
    void getUserByUsername_shouldReturnUser_ifUsernameIsValid() {
        // Arrange
        String username = "john";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setPassword("mockpassword");

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("username", username)).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenReturn(expectedUser);

        // Act
        Optional<User> result = userDao.getUserByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        assertEquals(expectedUser.getPassword(),result.get().getPassword());
    }

    @Test
    void getUserByUsername_shouldReturnEmptyOptional_ifUsernameIsNotValid() {
        // Arrange
        String username = "unknown";

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("username", username)).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenThrow(new RuntimeException("User not found"));

        // Act
        Optional<User> result = userDao.getUserByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserByProviderUID_shouldReturnUser_ifProviderUIDIsValid() {
        // Arrange
        String providerUID = "google-12345";
        User expectedUser = new User();
        expectedUser.setProviderUniqueId(providerUID);

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("providerUID", providerUID)).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenReturn(expectedUser);

        // Act
        Optional<User> result = userDao.getUserByProviderUID(providerUID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(providerUID, result.get().getProviderUniqueId());
    }

    @Test
    void getUserByProviderUID_shouldReturnEmptyOptional_ifProviderUIDIsNotValid() {
        // Arrange
        String providerUID = "nonexistent-uid";

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("providerUID", providerUID)).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenThrow(new RuntimeException("Not found"));

        // Act
        Optional<User> result = userDao.getUserByProviderUID(providerUID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void create_shouldPersistUserSuccessfully() {
        // Arrange
        User user = new User();
        // No mocking needed, assume persist doesn't throw anything

        // Act & Assert
        assertDoesNotThrow(() -> userDao.create(user));
        verify(entityManager, times(1)).persist(user);
    }

    @Test
    void emailExists_shouldReturnTrue_ifEmailExists(){
        //arrange
        String email = "test12@gmail.com";

        when(entityManager.createQuery(anyString(), eq(Boolean.class))).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.setParameter("email", email)).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.getSingleResult()).thenReturn(true);

        assertTrue(userDao.emailExists(email));
    }

    @Test
    void emailExists_shouldReturnFalse_ifEmailDoesNotExists(){
        //arrange
        String email = "unknown";

        when(entityManager.createQuery(anyString(), eq(Boolean.class))).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.setParameter("email", email)).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.getSingleResult()).thenReturn(false);

        assertFalse(userDao.emailExists("unknown"));
    }


    @Test
    void providerUIDExists_shouldReturnTrue_ifProviderUIDExists(){
        //arrange
        String providerUID = "mock-provider-uid";

        when(entityManager.createQuery(anyString(), eq(Boolean.class))).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.setParameter("providerUniqueId", providerUID)).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.getSingleResult()).thenReturn(true);

        assertTrue(userDao.providerUIDExists(providerUID));
    }

    @Test
    void providerUIDExists_shouldReturnFalse_ifProviderUIDDoesNotExists(){
        //arrange
        String providerUID = "unknown";

        when(entityManager.createQuery(anyString(), eq(Boolean.class))).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.setParameter("providerUniqueId", providerUID)).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.getSingleResult()).thenReturn(false);

        assertFalse(userDao.providerUIDExists("unknown"));
    }

    @Test
    void usernameExists_shouldReturnTrue_ifUsernameExists(){
        //arrange
        String username = "adriel14";

        when(entityManager.createQuery(anyString(),eq(Boolean.class))).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.setParameter("username",username)).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.getSingleResult()).thenReturn(true);

        assertTrue(userDao.usernameExists(username));
    }

    @Test
    void usernameExists_shouldReturnFalse_ifUsernameExists(){
        when(entityManager.createQuery(anyString(),eq(Boolean.class))).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.setParameter(eq("username"),anyString())).thenReturn(booleanTypedQuery);
        when(booleanTypedQuery.getSingleResult()).thenReturn(false);

        assertFalse(userDao.usernameExists("unknown"));
    }

}
