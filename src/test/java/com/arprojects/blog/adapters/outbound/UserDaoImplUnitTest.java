package com.arprojects.blog.adapters.outbound;

import com.arprojects.blog.adapters.outbound.repositories.UserDaoImpl;
import com.arprojects.blog.domain.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void shouldReturnUser_whenUserExists() {
        // Arrange
        String username = "john";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setPassword("mockpassword");

        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter(eq("username"), eq(username))).thenReturn(query);
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
        when(query.setParameter(eq("username"), eq(username))).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new RuntimeException("User not found"));

        // Act
        Optional<User> result = userDao.getUserByUsername(username);

        // Assert
        assertTrue(result.isEmpty());
    }

}
