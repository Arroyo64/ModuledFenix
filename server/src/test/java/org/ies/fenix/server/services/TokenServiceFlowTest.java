package org.ies.fenix.server.services;

import org.ies.fenix.server.models.AuthToken;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.repositories.AuthTokenRepository;
import org.ies.fenix.server.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceFlowTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void generateToken_whenClientExists_savesTokenLinkedToClient() {
        Client client = new Client();
        client.setId(1);
        client.setUsername("ana");

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String tokenValue = tokenService.generateToken(1);

        assertNotNull(tokenValue);
        assertEquals(64, tokenValue.length());

        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        AuthToken savedToken = tokenCaptor.getValue();
        assertEquals(tokenValue, savedToken.getToken());
        assertSame(client, savedToken.getUser());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()));
        assertNull(savedToken.getRevokedAt());
        verify(clientRepository).findById(1);
    }

    @Test
    void revoke_whenTokenExists_marksTokenAsRevokedAndSavesIt() {
        AuthToken token = AuthToken.builder()
                .token("token-123")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(authTokenRepository.findByToken("token-123")).thenReturn(Optional.of(token));
        when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tokenService.revoke("token-123");

        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getRevokedAt());
        verify(authTokenRepository).findByToken("token-123");
    }
}
