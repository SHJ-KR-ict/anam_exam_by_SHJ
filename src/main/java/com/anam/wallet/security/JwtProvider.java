package com.anam.wallet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

// JWT 생성 및 유효성 검증 컴포넌트
@Component
public class JwtProvider {
    private final Key key;
    private final long expiration;
    private final long refreshexpiration;
    
    // jwt 생성용 비밀 키 및 유효시간 초기화
    public JwtProvider(@Value("${jwt.secret}") String secret, 
                       @Value("${jwt.expiration}") long expiration,
                       @Value("${jwt.refreshexpiration}") long refreshexpiration) {
        if (secret == null || secret.length() < 32) throw new IllegalStateException("JWT Secret이 너무 짧습니다.");// 브루탈 포스 공격 방지
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshexpiration = refreshexpiration;
    }
    // 액세스 토큰 생성
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }
    // 리프레쉬 토큰 생성
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshexpiration)) // 1시간 적용
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // 토큰에서 유저이름 추출
    public String getUsernameFromToken(String token) {
    	Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // {"sub":username}
        return claims.getSubject();
    }
    
    // 토큰 유효성 검사
    public boolean validate(String token) {
        try { Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); return true; }
        catch (Exception e) { return false; }
    }
}