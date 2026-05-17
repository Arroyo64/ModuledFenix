package org.ies.fenix.server.services;

import org.ies.fenix.server.models.AuthToken;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.repositories.AuthTokenRepository;
import org.ies.fenix.server.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class TokenService {

    private static final long TOKEN_TTL_SECONDS = 60 * 60 * 24; // 24h
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AuthTokenRepository authTokenRepository;

    public String generateToken(Integer clientId) {
        String tokenValue = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        Client client = clientRepository.findById(clientId)
                .orElseThrow();

        AuthToken token = AuthToken.builder()
                .token(tokenValue)
                .user(client)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(TOKEN_TTL_SECONDS))
                .build();

        authTokenRepository.save(token);
        return tokenValue;
    }

    public boolean isValid(String tokenValue) {
        return authTokenRepository.findByToken(tokenValue)
                .filter(token -> token.getRevokedAt() == null)
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    public Client getClient(String tokenValue) {
        return authTokenRepository.findByToken(tokenValue)
                .filter(token -> token.getRevokedAt() == null)
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .map(AuthToken::getUser)
                .orElse(null);
    }

    public void revoke(String tokenValue) {
        authTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            authTokenRepository.save(token);
        });
    }
    public static String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}