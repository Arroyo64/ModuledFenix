package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.client.ClientLoginDTO;
import org.ies.fenix.controller.dto.client.ClientNameDTO;
import org.ies.fenix.controller.dto.client.ClientRegisterDTO;
import org.ies.fenix.controller.dto.client.LoginResponseDTO;
import org.ies.fenix.controller.dto.client.RegisterResponseDTO;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.services.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    @Test
    void register_whenDtoIsValid_returnsOkWithServiceResponse() {
        ClientRegisterDTO request = new ClientRegisterDTO("ana", "ana@mail.com", "1234");
        RegisterResponseDTO serviceResponse = RegisterResponseDTO.builder()
                .status("OK")
                .message("User registered successfully")
                .access(true)
                .build();
        when(clientService.register(request)).thenReturn(serviceResponse);

        ResponseEntity<RegisterResponseDTO> response = clientController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(clientService).register(request);
    }

    @Test
    void register_whenDtoIsNull_returnsBadRequestAndDoesNotCallService() {
        ResponseEntity<RegisterResponseDTO> response = clientController.register(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(clientService);
    }

    @Test
    void login_whenDtoIsValid_returnsOkWithServiceResponse() {
        ClientLoginDTO request = new ClientLoginDTO("ana", "1234");
        LoginResponseDTO serviceResponse = LoginResponseDTO.builder()
                .status("OK")
                .message("Login successful")
                .clientId(1)
                .username("ana")
                .token("token-123")
                .build();
        when(clientService.login(request)).thenReturn(serviceResponse);

        ResponseEntity<LoginResponseDTO> response = clientController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(clientService).login(request);
    }

    @Test
    void logout_whenAuthorizationHasBearerToken_callsServiceWithToken() {
        ResponseEntity<Void> response = clientController.logout("Bearer token-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clientService).logout("token-123");
    }

    @Test
    void logout_whenAuthorizationIsInvalid_doesNotCallService() {
        ResponseEntity<Void> response = clientController.logout("token-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verifyNoInteractions(clientService);
    }

    @Test
    void getUsername_whenAuthorizationHasBearerToken_returnsUsername() {
        Client client = new Client();
        client.setUsername("ana");
        when(clientService.getClient("token-123")).thenReturn(client);

        ResponseEntity<ClientNameDTO> response = clientController.getUsername("Bearer token-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ana", response.getBody().getUsername());
        verify(clientService).getClient("token-123");
    }

    @Test
    void getUsername_whenAuthorizationIsMissing_returnsBadRequest() {
        ResponseEntity<ClientNameDTO> response = clientController.getUsername(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(clientService);
    }
}
