package com.guanxuege.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与解析（HS256）
 */
public class JwtUtil {

    private static final String SECRET = "guanxuege-secret-key-2026-very-long-string-for-hs256";
    private static final long EXPIRE_MS = 2L * 60 * 60 * 1000; // 2 小时
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String createToken(Long userId, String phone) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("phone", phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public static boolean isExpired(String token) {
        return parse(token).getExpiration().before(new Date());
    }
}
