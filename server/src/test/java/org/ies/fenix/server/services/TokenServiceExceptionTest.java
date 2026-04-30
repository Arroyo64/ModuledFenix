package org.ies.fenix.server.services;

import org.ies.fenix.server.models.AuthToken;
import org.ies.fenix.server.repositories.AuthTokenRepository;
import org.ies.fenix.server.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceExceptionTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void generateToken_whenClientDoesNotExist_throwsNoSuchElementExceptionAndDoesNotSaveToken() {
        when(clientRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> tokenService.generateToken(99)
        );

        verify(clientRepository).findById(99);
        verify(authTokenRepository, never()).save(any(AuthToken.class));
    }

    @Test
    void isValid_whenRepositoryRejectsToken_propagatesIllegalArgumentException() {
        when(authTokenRepository.findByToken("bad-token"))
                .thenThrow(new IllegalArgumentException("token format is invalid"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.isValid("bad-token")
        );

        assertEquals("token format is invalid", exception.getMessage());
        verify(authTokenRepository).findByToken("bad-token");
    }
}
