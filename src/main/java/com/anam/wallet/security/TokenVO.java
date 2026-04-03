package com.anam.wallet.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenVO {
    private String accessToken;
    private String refreshToken;
}