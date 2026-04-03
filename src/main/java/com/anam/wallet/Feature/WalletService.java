package com.anam.wallet.Feature;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final Web3j web3j;
    
    // 동일한 주소에서 반복요청시 rpc를 거치지않고 캐시 제공
    @Cacheable(value = "balanceCache", key = "#address")
    public String getBalance(String address) throws Exception {
    	System.out.println("RPC 노드 사용함");
    	// 이더리움 네트워크에서 address(계좌)의 최신 balance(잔액)정보조회
        var wei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        // ETHER로 단위변환
        return Convert.fromWei(wei.toString(), Convert.Unit.ETHER).toPlainString();
    }
}
