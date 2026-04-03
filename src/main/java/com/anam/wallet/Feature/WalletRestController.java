package com.anam.wallet.Feature;

import com.anam.wallet.security.JwtProvider;
import com.anam.wallet.security.TokenVO;
import com.anam.wallet.Feature.WalletService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WalletRestController {
    private final WalletService walletService;
    private final JwtProvider jwtProvider;

    // POST 요청 시 새로운 JWT 토큰을 문자열로 반환
    // 실제로는 프론트에서 로그인 시 마지막에 요청하여 이를 브라우저의 로컬 스토리지에 저장함
    @PostMapping("/auth/token")
    public TokenVO getToken(@RequestBody Map<String, String> request) {
    	String username = request.get("username");
    	
    	String accesstoken = jwtProvider.generateAccessToken(username);
    	String refreshToken = jwtProvider.generateRefreshToken(username);
    	
    	TokenVO tokenvo = new TokenVO();
    	tokenvo.setAccessToken(accesstoken);
    	tokenvo.setRefreshToken(refreshToken);
        return tokenvo;
    }
    
    @PostMapping("/auth/refresh")
    public TokenVO refreshToken(@RequestBody TokenVO requestTokenVO) {
        String refreshToken = requestTokenVO.getRefreshToken();
        // 토큰 유효성 검사
        if (refreshToken != null && jwtProvider.validate(refreshToken)) {
            // username 확인
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            // access 새로 생성
            String newAccessToken = jwtProvider.generateAccessToken(username); 
            // 4. 새로 액세스 토큰
            TokenVO responseVO = new TokenVO();
            responseVO.setAccessToken(newAccessToken);
            responseVO.setRefreshToken(refreshToken); //리프레쉬 토큰 유지
            return responseVO;
        } else {
            // 토큰 유효하지 않음
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다. 다시 로그인하세요.");
        }
    }

     // SecurityConfig에 의해 인증된 사용자만 호출 가능
    @GetMapping("/balance/{address}")
    public ResponseEntity<String> getBalance(@PathVariable("address") String address) {
        try {
            return ResponseEntity.ok(walletService.getBalance(address));
        } catch (Exception e) {

            return ResponseEntity.badRequest().body("조회 중 오류 발생: " + e.getMessage());
        }
    }
}