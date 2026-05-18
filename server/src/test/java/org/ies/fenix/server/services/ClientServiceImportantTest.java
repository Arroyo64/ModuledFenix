package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.ServerResponseDTO;
import org.ies.fenix.controller.dto.client.ClientLoginDTO;
import org.ies.fenix.controller.dto.client.ClientRegisterDTO;
import org.ies.fenix.controller.dto.client.LoginResponseDTO;
import org.ies.fenix.controller.dto.client.RegisterResponseDTO;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImportantTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private ClientService clientService;

    @Test
    void register_whenDataIsValid_savesClientAndReturnsOk() {
        ClientRegisterDTO request = new ClientRegisterDTO("david", "david@test.com", "123456");

        when(clientRepository.findByUsername("david")).thenReturn(Optional.empty());
        when(clientRepository.findByEmail("david@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("HASHED");
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponseDTO response = clientService.register(request);

        assertTrue(response.isAccess());
        assertEquals("OK", response.getStatus());
        assertEquals("User registered successfully", response.getMessage());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());

        Client saved = captor.getValue();
        assertEquals("david", saved.getUsername());
        assertEquals("david@test.com", saved.getEmail());
        assertEquals("HASHED", saved.getPasswordHashed());
        assertEquals(5, saved.getCharacterCounterPassword());
        assertNull(saved.getBio());
    }

    @Test
    void register_whenUsernameAlreadyExists_returnsWarn() {
        ClientRegisterDTO request = new ClientRegisterDTO("david", "david@test.com", "123456");

        when(clientRepository.findByUsername("david"))
                .thenReturn(Optional.of(new Client()));

        RegisterResponseDTO response = clientService.register(request);

        assertFalse(response.isAccess());
        assertEquals("WARN", response.getStatus());
        assertEquals("This username already exists", response.getMessage());

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void login_whenCredentialsAreCorrect_returnsToken() {
        ClientLoginDTO request = new ClientLoginDTO("david", "123456");

        Client client = new Client();
        client.setId(1);
        client.setUsername("david");
        client.setPasswordHashed("HASHED");

        when(clientRepository.findByUsername("david"))
                .thenReturn(Optional.of(client));
        when(passwordEncoder.matches("123456", "HASHED"))
                .thenReturn(true);
        when(tokenService.generateToken(1))
                .thenReturn("token-123");

        LoginResponseDTO response = clientService.login(request);

        assertEquals("OK", response.getStatus());
        assertEquals("Login successful", response.getMessage());
        assertEquals(1, response.getClientId());
        assertEquals("david", response.getUsername());
        assertEquals("token-123", response.getToken());
    }

    @Test
    void login_whenPasswordIsWrong_returnsWarn() {
        ClientLoginDTO request = new ClientLoginDTO("david", "wrong");

        Client client = new Client();
        client.setId(1);
        client.setUsername("david");
        client.setPasswordHashed("HASHED");

        when(clientRepository.findByUsername("david"))
                .thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrong", "HASHED"))
                .thenReturn(false);

        LoginResponseDTO response = clientService.login(request);

        assertEquals("WARN", response.getStatus());
        assertEquals("Password incorrect", response.getMessage());
        assertNull(response.getToken());

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void getClient_whenBearerTokenIsValid_returnsClient() {
        Client client = new Client();
        client.setId(1);
        client.setUsername("david");

        when(tokenService.isValid("token-123")).thenReturn(true);
        when(clientRepository.findByAuthTokensToken("token-123")).thenReturn(client);

        Client response = clientService.getClient("Bearer token-123");

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("david", response.getUsername());
    }

    @Test
    void getClient_whenTokenIsInvalid_returnsNull() {
        when(tokenService.isValid("token-123")).thenReturn(false);

        Client response = clientService.getClient("Bearer token-123");

        assertNull(response);
        verify(clientRepository, never()).findByAuthTokensToken(any());
    }

    @Test
    void updateBio_whenTokenIsValid_savesBio() {
        Client client = new Client();
        client.setId(1);
        client.setUsername("david");

        when(tokenService.isValid("token-123")).thenReturn(true);
        when(clientRepository.findByAuthTokensToken("token-123")).thenReturn(client);

        ServerResponseDTO response = clientService.updateBio("Bearer token-123", "Nueva bio");

        assertEquals("OK", response.getStatus());
        assertEquals("Bio updated successfully", response.getMessage());
        assertEquals("Nueva bio", client.getBio());

        verify(clientRepository).save(client);
    }
}