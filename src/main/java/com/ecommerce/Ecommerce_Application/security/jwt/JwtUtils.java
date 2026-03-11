package com.ecommerce.Ecommerce_Application.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {

    @Value("${spring.app.jwtSecret}")
    private String secretKey;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );
    }

    public boolean checkTokenValidation(String jwtToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (Exception e) {
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
}
