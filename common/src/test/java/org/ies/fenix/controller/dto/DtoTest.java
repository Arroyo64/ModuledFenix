package org.ies.fenix.controller.dto;

import org.ies.fenix.controller.dto.client.ClientLoginDTO;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.ies.fenix.controller.dto.client.ClientRegisterDTO;
import org.ies.fenix.controller.dto.client.LoginResponseDTO;
import org.ies.fenix.controller.dto.client.RegisterResponseDTO;
import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DtoTest {

    @Test
    void clientDtos_storeValuesFromConstructorsAndSetters() {
        ClientLoginDTO login = new ClientLoginDTO("david", "secret");
        assertEquals("david", login.getUsername());
        assertEquals("secret", login.getPassword());

        ClientRegisterDTO register = new ClientRegisterDTO();
        register.setUsername("ana");
        register.setEmail("ana@fenix.local");
        register.setPassword("1234");

        assertEquals("ana", register.getUsername());
        assertEquals("ana@fenix.local", register.getEmail());
        assertEquals("1234", register.getPassword());

        ClientInfoDTO clientName = new ClientInfoDTO("player1",null,0);
        assertEquals("player1", clientName.getUsername());
    }

    @Test
    void responseDtos_storeInheritedAndOwnFields() {
        ServerResponseDTO serverResponse = new ServerResponseDTO("OK", "Done");
        assertEquals("OK", serverResponse.getStatus());
        assertEquals("Done", serverResponse.getMessage());

        LoginResponseDTO loginResponse = new LoginResponseDTO();
        loginResponse.setStatus("OK");
        loginResponse.setMessage("Logged in");
        loginResponse.setClientId(7);
        loginResponse.setUsername("david");
        loginResponse.setToken("token-123");

        assertEquals("OK", loginResponse.getStatus());
        assertEquals("Logged in", loginResponse.getMessage());
        assertEquals(7, loginResponse.getClientId());
        assertEquals("david", loginResponse.getUsername());
        assertEquals("token-123", loginResponse.getToken());

        RegisterResponseDTO registerResponse = new RegisterResponseDTO();
        registerResponse.setAccess(true);
        assertEquals(true, registerResponse.isAccess());
    }
}
