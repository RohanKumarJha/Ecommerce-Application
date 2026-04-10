package com.ecommerce.core.security.jwt;

import com.ecommerce.core.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    private final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug("JWT token extracted from header");
            return header.substring(7);
        }
        return null;
    }

    public boolean validateToken(String jwtToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public String getUserNameFromToken(String jwtToken) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload()
                .getSubject();
    }

    public String generateJwtTokenFromUserName(UserDetailsImpl userDetails) {
        return Jwts.builder()
                .signWith(key())
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime()) + jwtExpirationMs))
                .compact();
    }

}
