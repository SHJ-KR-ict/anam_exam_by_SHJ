package com.anam.wallet.conifg;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

	//Caffeine 캐시 매니저를 빈으로 등록, rpc 자원 소모 방지
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 캐시 세부 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // 최대 1,000개의 지갑 주소 잔액 저장
                .expireAfterWrite(10, TimeUnit.SECONDS)); // 데이터 생성 후 10초 뒤 만료
                
        // 사용할 캐시 이름 지정
        //@Cacheable에서 사용
        cacheManager.setCacheNames(Collections.singletonList("balanceCache"));
        
        return cacheManager;
    }
}
