//package com.example.HNG_Security.utils;
//
//import com.example.HNG_Security.model.User;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Date;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(SpringExtension.class) // Ensure SpringExtension is used if needed
//@TestPropertySource(locations = "classpath:application.properties") // Load properties file if needed
//public class JwtUtilsTest {
//
//    @InjectMocks
//    private JwtUtils jwtUtils;
//
//    @Value("${sha512.string}")
//    private String secretKey;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testTokenGeneration() {
//        // Given
//        User user = new User();
//        user.setUserId("userId");
//        user.setFirstName("John");
//        user.setLastName("Doe");
//        user.setEmail("john.doe@example.com");
//
//        // When
//        String token = jwtUtils.createJwt.apply(user);
//
//        // Then
//        assertNotNull(token);
//        assertTrue(token.length() > 0);
//
//
//        Claims claims = Jwts.parser()
//                .verifyWith(getSecretKey())
//                .build().parseSignedClaims(token).getPayload();
//
//        assertEquals(user.getUsername(), claims.getSubject());
//        assertEquals(user.getId(), claims.get("id"));
//        assertEquals(user.getFirstName(), claims.get("firstName"));
//        assertEquals(user.getLastName(), claims.get("lastName"));
//    }
//
//    @Test
//    void testTokenExpiration() {
//        // Given
//        User user = new User();
//        user.setEmail("john.doe@example.com");
//
//        // When
//        String token = jwtUtils.createJwt.apply(user);
//
//        // Then
//        assertNotNull(token);
//
//
//        Date expirationDate = Jwts.parser()
//                .verifyWith(getSecretKey())
//                .build().parseSignedClaims(token).getPayload()
//                .getExpiration();
//
//        assertTrue(expirationDate.after(new Date()));
//    }
//
//    private SecretKey getSecretKey() {
//        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
//        return new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
//    }
//}
