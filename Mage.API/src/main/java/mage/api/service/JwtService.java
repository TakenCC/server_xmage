package mage.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.UUID;

public class JwtService {

    private static final Logger logger = Logger.getLogger(JwtService.class);
    private static final String SECRET_KEY = System.getProperty("jwt.secret", "mage-secret-key-change-in-production");
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 horas en milisegundos
    private static final String ISSUER = "mage-server";

    public String generateToken(String username, String sessionId) {
        try {
            Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(username)
                    .withClaim("sessionId", sessionId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(expirationDate)
                    .withJWTId(UUID.randomUUID().toString())
                    .sign(ALGORITHM);
        } catch (JWTCreationException ex) {
            logger.error("Error generating JWT token", ex);
            throw new RuntimeException("Error generating token", ex);
        }
    }

    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(ALGORITHM)
                    .withIssuer(ISSUER)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            logger.debug("Invalid JWT token: " + ex.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT decoded = verifyToken(token);
        if (decoded != null) {
            return decoded.getSubject();
        }
        return null;
    }

    public String getSessionIdFromToken(String token) {
        DecodedJWT decoded = verifyToken(token);
        if (decoded != null) {
            return decoded.getClaim("sessionId").asString();
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        return verifyToken(token) != null;
    }
}

