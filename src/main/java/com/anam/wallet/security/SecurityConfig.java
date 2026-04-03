package com.anam.wallet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

// 애플리케이션 보안정책 필터체인 제공
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // JWT 사용 > STATELESS > 쿠키 필요없음 > CSRF 공격 받지않음
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 생성하지않음, jwt만 볼것
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/auth/token","/api/auth/refresh").permitAll() // 메인화면 및 토큰생성은 누구나 가능
                .anyRequest().authenticated() // 이외의 요청은 인증 필요
            )// 필터 두번 실행 방지
            .addFilterBefore(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) 
                        throws ServletException, IOException {
                	// 헤더 Bearer 토큰 확인
                    String authHeader = req.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        // 토큰 유효할 시 스프링 시큐리티에 인증상태 저장
                        try {
                            if (jwtProvider.validate(token)) {
                                SecurityContextHolder.getContext().setAuthentication(
                                    new UsernamePasswordAuthenticationToken("User", null, Collections.emptyList()));
                            }
                        // Access Token 만료시 401
                        } catch (io.jsonwebtoken.ExpiredJwtException e) { 
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("Access Token Expired");
                            return;
                        } catch (Exception e) {
                            // 조작되거나 이상한 토큰인 경우 403
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().write("Invalid Token");
                            return;
                        }
                    }
                    // 다음 필터로
                    chain.doFilter(req, res);
                }
            }, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
