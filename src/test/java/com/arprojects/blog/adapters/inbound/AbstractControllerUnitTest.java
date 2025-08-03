package com.arprojects.blog.adapters.inbound;


import com.arprojects.blog.infrastructure.security.ArBlogSecurityConfig;
import com.arprojects.blog.infrastructure.security.RsaKeyProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@Import(ArBlogSecurityConfig.class)
public class AbstractControllerUnitTest {

    // 🔧 All beans required by ArBlogSecurityConfig
    @MockitoBean
    protected UserDetailsService userDetailsService;

    @MockitoBean
    protected PasswordEncoder passwordEncoder;

    @MockitoBean
    protected JwtEncoder jwtEncoder;

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    protected RsaKeyProperties rsaKeyProperties;

    @MockitoBean
    protected RestTemplate restTemplate;
}
