package org.ies.fenix.controller.dto;

import org.ies.fenix.controller.dto.client.ClientLoginDTO;
import org.ies.fenix.controller.dto.client.ClientInfoDTO;
import org.ies.fenix.controller.dto.client.ClientRegisterDTO;
import org.ies.fenix.controller.dto.client.LoginResponseDTO;
import org.ies.fenix.controller.dto.client.RegisterResponseDTO;
import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.controller.dto.purchase.DownloadResponseDTO;
import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseResponseDTO;
import org.ies.fenix.controller.dto.tag.TagResponseDTO;
import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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

    @Test
    void gameDtos_storeSearchAndResponseValues() {
        GameSearchDTO search = new GameSearchDTO("Fenix", "dev", List.of("rpg", "demo"), 10);
        assertEquals("Fenix", search.getTitle());
        assertEquals("dev", search.getDeveloperName());
        assertEquals(List.of("rpg", "demo"), search.getTagNames());
        assertEquals(10, search.getLimit());

        GameResponseDTO response = new GameResponseDTO(
                1,
                "Fenix Game",
                "A visual novel",
                "512 MB",
                "2K",
                BigDecimal.valueOf(4.99),
                "devUser",
                List.of("visual-novel")
        );

        assertEquals(1, response.getId());
        assertEquals("Fenix Game", response.getTitle());
        assertEquals("A visual novel", response.getDescription());
        assertEquals("512 MB", response.getSizeApproximation());
        assertEquals("2K", response.getDownloadsApproximation());
        assertEquals(BigDecimal.valueOf(4.99), response.getPrice());
        assertEquals("devUser", response.getDevUsername());
        assertEquals(List.of("visual-novel"), response.getTags());
    }

    @Test
    void purchaseDtos_storePurchaseLibraryAndDownloadValues() {
        PurchaseCreateDTO create = new PurchaseCreateDTO(3, 9);
        assertEquals(3, create.getClientId());
        assertEquals(9, create.getGameId());

        PurchaseResponseDTO purchase = new PurchaseResponseDTO(
                1,
                3,
                9,
                "Fenix Game",
                BigDecimal.valueOf(4.99)
        );
        assertEquals(1, purchase.getId());
        assertEquals(3, purchase.getClientId());
        assertEquals(9, purchase.getGameId());
        assertEquals("Fenix Game", purchase.getGameTitle());
        assertEquals(BigDecimal.valueOf(4.99), purchase.getPrice());

        LibraryGameDTO libraryGame = new LibraryGameDTO(
                9,
                "Fenix Game",
                "Description",
                BigDecimal.valueOf(512),
                20,
                BigDecimal.valueOf(4.99)
        );
        assertEquals(9, libraryGame.getGameId());
        assertEquals("Fenix Game", libraryGame.getTitle());
        assertEquals(BigDecimal.valueOf(512), libraryGame.getTamanoMb());
        assertEquals(20, libraryGame.getDownloads());

        DownloadResponseDTO download = new DownloadResponseDTO(9, "Fenix Game", 21);
        assertEquals(9, download.getGameId());
        assertEquals("Fenix Game", download.getTitle());
        assertEquals(21, download.getDownloads());
    }

    @Test
    void simpleCatalogDtos_storeValuesAndTeaserHasEqualsHashCode() {
        TagResponseDTO tag = new TagResponseDTO(1, "rpg", "Role playing game");
        assertEquals(1, tag.getId());
        assertEquals("rpg", tag.getName());
        assertEquals("Role playing game", tag.getDescription());

        TeaserResponseDTO first = new TeaserResponseDTO();
        first.setId(1);
        first.setGameId(9);
        first.setFileName("trailer.mp4");
        first.setType("video");

        TeaserResponseDTO same = new TeaserResponseDTO();
        same.setId(1);
        same.setGameId(9);
        same.setFileName("trailer.mp4");
        same.setType("video");

        TeaserResponseDTO different = new TeaserResponseDTO();
        different.setId(2);
        different.setGameId(9);
        different.setFileName("image.png");
        different.setType("image");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
    }
}
