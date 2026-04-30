package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.purchase.DownloadResponseDTO;
import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseResponseDTO;
import org.ies.fenix.server.exception.AlreadyPurchasedException;
import org.ies.fenix.server.services.PurchaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController purchaseController;

    @Test
    void createPurchase_whenPurchaseIsCreated_returnsOkWithPurchase() {
        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 2);
        PurchaseResponseDTO serviceResponse = aPurchaseResponse();
        when(purchaseService.createPurchase(request)).thenReturn(serviceResponse);

        ResponseEntity<?> response = purchaseController.createPurchase(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(purchaseService).createPurchase(request);
    }

    @Test
    void createPurchase_whenServiceReturnsNull_returnsNotFound() {
        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 99);
        when(purchaseService.createPurchase(request)).thenReturn(null);

        ResponseEntity<?> response = purchaseController.createPurchase(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(purchaseService).createPurchase(request);
    }

    @Test
    void createPurchase_whenAlreadyPurchased_returnsConflictMessage() {
        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 2);
        when(purchaseService.createPurchase(request))
                .thenThrow(new AlreadyPurchasedException("Game already purchased"));

        ResponseEntity<?> response = purchaseController.createPurchase(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Game already purchased", response.getBody());
        verify(purchaseService).createPurchase(request);
    }

    @Test
    void createPurchase_whenServiceThrowsUnexpectedException_returnsBadRequest() {
        PurchaseCreateDTO request = new PurchaseCreateDTO(1, 2);
        when(purchaseService.createPurchase(request)).thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<?> response = purchaseController.createPurchase(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(purchaseService).createPurchase(request);
    }

    @Test
    void getByClientId_returnsOkWithPurchases() {
        List<PurchaseResponseDTO> purchases = List.of(aPurchaseResponse());
        when(purchaseService.getByClientId(1)).thenReturn(purchases);

        ResponseEntity<List<PurchaseResponseDTO>> response = purchaseController.getByClientId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(purchases, response.getBody());
        verify(purchaseService).getByClientId(1);
    }

    @Test
    void getByClientId_whenServiceThrowsException_returnsBadRequest() {
        when(purchaseService.getByClientId(1)).thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<List<PurchaseResponseDTO>> response = purchaseController.getByClientId(1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(purchaseService).getByClientId(1);
    }

    @Test
    void getLibraryByClientId_returnsOkWithLibraryGames() {
        List<LibraryGameDTO> library = List.of(
                new LibraryGameDTO(2, "Fenix", "Visual novel", BigDecimal.valueOf(512), 100, BigDecimal.TEN)
        );
        when(purchaseService.getLibraryByClientId(1)).thenReturn(library);

        ResponseEntity<List<LibraryGameDTO>> response = purchaseController.getLibraryByClientId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(library, response.getBody());
        verify(purchaseService).getLibraryByClientId(1);
    }

    @Test
    void downloadGame_whenServiceReturnsDownload_returnsOk() {
        DownloadResponseDTO download = new DownloadResponseDTO(2, "Fenix", 101);
        when(purchaseService.downloadGame(1, 2)).thenReturn(download);

        ResponseEntity<DownloadResponseDTO> response = purchaseController.downloadGame(1, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(download, response.getBody());
        verify(purchaseService).downloadGame(1, 2);
    }

    @Test
    void downloadGame_whenServiceReturnsNull_returnsNotFound() {
        when(purchaseService.downloadGame(1, 99)).thenReturn(null);

        ResponseEntity<DownloadResponseDTO> response = purchaseController.downloadGame(1, 99);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(purchaseService).downloadGame(1, 99);
    }

    @Test
    void downloadGame_whenServiceThrowsException_returnsBadRequest() {
        when(purchaseService.downloadGame(1, 2)).thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<DownloadResponseDTO> response = purchaseController.downloadGame(1, 2);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(purchaseService).downloadGame(1, 2);
    }

    private PurchaseResponseDTO aPurchaseResponse() {
        return new PurchaseResponseDTO(10, 1, 2, "Fenix", BigDecimal.TEN);
    }
}
