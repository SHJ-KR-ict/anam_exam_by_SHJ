package com.anam.wallet.Feature;

import com.anam.wallet.Feature.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class WalletViewController {

    private final WalletService walletService;

    @GetMapping("/")
    public String index() {
        return "index"; // templates/index.html
    }
    
    // templates/index.html 로 포워딩
    @PostMapping("/balance")
    public String getBalancePage(@RequestParam("address") String address, Model model) {
        try {
            String balance = walletService.getBalance(address);
            model.addAttribute("address", address);
            model.addAttribute("balance", balance);
        } catch (Exception e) {
            model.addAttribute("error", "조회 실패: " + e.getMessage());
        }
        return "index";
    }
}
