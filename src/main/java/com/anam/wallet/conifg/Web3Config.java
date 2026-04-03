package com.anam.wallet.conifg;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

// rpc 노드 연결관리
@Configuration
public class Web3Config {
	// 환경변수 주입
    @Value("${web3.rpc-url}")
    private String rpcUrl;
    
    // 노드 접근시 사용할 web3 객체를 빈으로 등록
    // 서비스 계층에서 사용
    @Bean
    public Web3j web3j() {
        // TCP 연결을 재사용하기 위한 (Keep-Alive) 커넥션 풀 설정
        // 최대 5개의 연결 5분 동안 미사용시 정리
        ConnectionPool pool = new ConnectionPool(5, 5, TimeUnit.MINUTES);

        // 커넥션 풀 사용위한 OkHttpClient 객체 생성
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(5, TimeUnit.SECONDS) // 노드 연결 최대 5초 대기
                .readTimeout(5, TimeUnit.SECONDS)    // RPC 응답 최대 5초 대기
                .build();

        // 커스텀 클라이언트를 사용하는 HttpService로 Web3j 빌드
        return Web3j.build(new HttpService(rpcUrl, okHttpClient));
    }
}