package kz.moon.app.config;

import io.jsonwebtoken.*;
import kz.moon.app.seclevel.domain.Role;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Component
public class JwtTokenProvider {

    private final String jwtSecret = "mySuperSecretKeyChangeMe";
    private final long jwtExpirationMs = 3600000; // 1 hour

    public String createToken(String username, Set<Role> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles.stream().map(r -> r.getName().name()).toList());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}