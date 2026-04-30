package org.ies.fenix.server.services;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceFlowTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private ClientService clientService;

    @Test
    void register_whenDataIsValid_encodesPasswordSavesClientAndReturnsOk() {
        ClientRegisterDTO request = new ClientRegisterDTO("ana", "ana@example.com", "secret");

        when(clientRepository.findByUsername("ana")).thenReturn(Optional.empty());
        when(clientRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("HASHED_PASSWORD");
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponseDTO response = clientService.register(request);

        assertTrue(response.isAccess());
        assertEquals("OK", response.getStatus());
        assertEquals("User registered successfully", response.getMessage());

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        Client savedClient = clientCaptor.getValue();
        assertEquals("ana", savedClient.getUsername());
        assertEquals("ana@example.com", savedClient.getEmail());
        assertEquals("HASHED_PASSWORD", savedClient.getPasswordHashed());
        assertNull(savedClient.getBio());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void login_whenCredentialsAreValid_generatesTokenAndReturnsLoginDto() {
        ClientLoginDTO request = new ClientLoginDTO("ana", "secret");
        Client client = new Client();
        client.setId(1);
        client.setUsername("ana");
        client.setPasswordHashed("HASHED_PASSWORD");

        when(clientRepository.findByUsername("ana")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("secret", "HASHED_PASSWORD")).thenReturn(true);
        when(tokenService.generateToken(1)).thenReturn("token-123");

        LoginResponseDTO response = clientService.login(request);

        assertEquals("OK", response.getStatus());
        assertEquals("Login successful", response.getMessage());
        assertEquals(1, response.getClientId());
        assertEquals("ana", response.getUsername());
        assertEquals("token-123", response.getToken());
        verify(clientRepository).findByUsername("ana");
        verify(passwordEncoder).matches("secret", "HASHED_PASSWORD");
        verify(tokenService).generateToken(1);
    }
}
