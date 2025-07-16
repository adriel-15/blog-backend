package com.arprojects.blog.application;

import com.arprojects.blog.domain.dtos.CustomUserDetails;
import com.arprojects.blog.domain.entities.Authority;
import com.arprojects.blog.domain.entities.User;
import com.arprojects.blog.domain.enums.Authorities;
import com.arprojects.blog.ports.outbound.repository_contracts.UserDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceUnitTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    @DisplayName("Should return UserDetails when user exists")
    void shouldReturnUserDetails_whenUserExists(){
        //Arrange
        String userRole = "ROLE_"+Authorities.ADMIN.getLabel();
        User user = new User();
        user.setId(1);
        user.setUsername("john");
        user.setPassword("secret");
        user.setEnabled(true);
        user.setAuthorities(Set.of(new Authority(Authorities.ADMIN)));

        when(userDao.getUserByUsername(user.getUsername())).thenReturn(Optional.of(user));

        //Act
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(user.getUsername());

        //Assert
        assertNotNull(userDetails);
        assertEquals(userDetails.getUserId(),user.getId());
        assertEquals(userDetails.getUsername(),user.getUsername());
        assertEquals(userDetails.getPassword(),user.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(userRole)));
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void shouldThrowException_whenUserDoesNotExist() {
        // Arrange
        when(userDao.getUserByUsername("unknown")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailService.loadUserByUsername("unknown"));

        verify(userDao, times(1)).getUserByUsername("unknown");
    }
}
