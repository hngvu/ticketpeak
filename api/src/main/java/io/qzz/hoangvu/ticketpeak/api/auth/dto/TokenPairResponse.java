package io.qzz.hoangvu.ticketpeak.api.auth.dto;

import io.qzz.hoangvu.ticketpeak.api.security.JwtTokenPair;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {

    public static TokenPairResponse from(JwtTokenPair tokenPair, long accessTokenExpiresIn, long refreshTokenExpiresIn) {
        return new TokenPairResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                "Bearer",
                accessTokenExpiresIn,
                refreshTokenExpiresIn
        );
    }
}
