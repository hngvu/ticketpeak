package io.qzz.hoangvu.ticketpeak.api.security;

public record JwtTokenPair(
        String accessToken,
        String refreshToken,
        String refreshTokenId
) {
}
