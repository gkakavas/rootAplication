package com.example.app.services.jwt;

import com.example.app.entities.Role;
import com.example.app.entities.User;
import com.example.app.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.when;
public class JwtServicePositiveUnitTest {
    private JwtService jwtService;
    @Mock
    private JwtService mockJwtService;
    private static final String signInKey = "BD7D471F77CFA2643C25D86B681B45N6952PT770YR6PR8E3VF1NY45W";
    private final byte[] keyBytes = Decoders.BASE64.decode(signInKey);
    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService();
    }
    private static final User user = User.builder()
            .userId(UUID.randomUUID())
            .password("testPassword")
            .firstname("testFirstname")
            .lastname("testLastname")
            .email("test@email.com")
            .specialization("testSpecialization")
            .currentProject("testCurrentProject")
            .role(Role.USER)
            .build();

    private final String token = Jwts.builder()
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis()+1000*60*24))
            .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
            .compact();

    @Test
    @DisplayName("Should extract username for the given token")
    void shouldExtractUsernameForTheGivenToken() {
        when(mockJwtService.extractClaim(token,Claims::getSubject)).thenReturn(user.getUsername());
        var actualUsername = jwtService.extractUsername(token);
        var expectedUsername = user.getUsername();
        Assertions.assertEquals(expectedUsername,actualUsername);
    }

    @Test
    @DisplayName("Should extract subject claim for the given token")
    public void shouldExtractSubjectClaimForTheGivenToken() {
        var expectedSubClaim = user.getUsername();
        when(mockJwtService.extractClaim(token, Claims::getSubject)).thenReturn(expectedSubClaim);

        String actualClaim = jwtService.extractClaim(token, Claims::getSubject);
        Assertions.assertEquals(expectedSubClaim, actualClaim);
    }
    @Test
    @DisplayName("Should generate token with username in subject claim")
    void shouldGenerateTokenOnlyWithUsernameInSubjectClaim(){
        when(mockJwtService.generateToken(new HashMap<>(), user)).thenReturn(token);
        var actualGeneratedToken = jwtService.generateToken(user);
        Claims mockClaims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Claims actualClaims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(actualGeneratedToken)
                .getBody();
        Long mockTokenIAT = mockClaims.getIssuedAt().getTime();
        Long mockTokenExp = mockClaims.getExpiration().getTime();
        Long actualTokenIAT = actualClaims.getIssuedAt().getTime();
        Long actualTokenExp = actualClaims.getExpiration().getTime();
        /*
          Asserting that the differences between issued and expiration
          of both tokens mock and actual that are equals
         */
        Assertions.assertEquals(1440000L, mockTokenExp - mockTokenIAT);
        Assertions.assertEquals(1440000L,actualTokenExp - actualTokenIAT);
        Assertions.assertEquals(mockClaims.getSubject(),actualClaims.getSubject());
    }
    @Test
    @DisplayName("Should generate token with username in subject claim and anything other custom claim")
    void shouldGenerateTokenWithUsernameInSubjectClaimAndAnythingOtherCustomClaim(){
        var customClaim = Instancio.createMap(String.class, String.class);
        var user = Instancio.create(User.class);
        var expectedResult =Jwts.builder()
                .setClaims(customClaim)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*24))
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                .compact();
        var result = jwtService.generateToken(customClaim,user);
        Assertions.assertEquals(expectedResult,result);
    }
    @Test
    @DisplayName("Should check if the given token is valid")
    void shouldCheckIfTheGivenTokenIsValid(){
        when(mockJwtService.extractUsername(token)).thenReturn(user.getUsername());
        when(mockJwtService.isTokenExpired(token)).thenReturn(false);
        var expectedResult = true;
        var result = jwtService.isTokenValid(token,user);
        Assertions.assertEquals(expectedResult,result);
    }
}

