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
 * <p>
 * 密钥优先取自环境变量 {@code JWT_SECRET}，长度需 >= 32 字符（HS256 要求 256 bit）。
 * 未配置或长度不足时回退到内置默认密钥并在启动日志告警 —— 默认密钥只应存在于本地开发，
 * 生产部署必须通过环境变量注入独立生成的随机串。
 */
public class JwtUtil {

    /** 本地开发兜底密钥，生产环境严禁使用 */
    private static final String DEFAULT_SECRET = "guanxuege-secret-key-2026-very-long-string-for-hs256";

    private static final int MIN_SECRET_LENGTH = 32;

    public static final String ENV_KEY = "JWT_SECRET";

    private static final long EXPIRE_MS = 2L * 60 * 60 * 1000; // 2 小时

    private static final String SECRET = resolveSecret();

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

    /** 是否正在使用内置默认密钥（生产环境应返回 false） */
    public static boolean usingDefaultSecret() {
        return DEFAULT_SECRET.equals(SECRET);
    }

    private static String resolveSecret() {
        String fromEnv = System.getenv(ENV_KEY);
        if (isValid(fromEnv)) {
            return fromEnv.trim();
        }
        String fromProp = System.getProperty("jwt.secret");
        if (isValid(fromProp)) {
            return fromProp.trim();
        }
        boolean configuredButWeak = (fromEnv != null && !fromEnv.isBlank())
                || (fromProp != null && !fromProp.isBlank());
        if (configuredButWeak) {
            warn("已配置 JWT_SECRET 但长度不足 " + MIN_SECRET_LENGTH + " 字符，HS256 要求密钥至少 256 bit，已回退默认密钥。");
        } else {
            warn("未检测到环境变量 JWT_SECRET，正在使用内置默认密钥。生产环境请务必单独设置，否则任何人可伪造登录凭证。");
        }
        return DEFAULT_SECRET;
    }

    private static boolean isValid(String secret) {
        return secret != null && secret.trim().length() >= MIN_SECRET_LENGTH;
    }

    private static void warn(String message) {
        System.err.println("[SECURITY][JwtUtil] " + message);
    }
}
