package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;
import org.ies.fenix.server.services.TeaserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeaserControllerTest {

    @Mock
    private TeaserService teaserService;

    @InjectMocks
    private TeaserController teaserController;

    @Test
    void getByGameId_whenTypeIsNull_callsServiceWithoutType() {
        List<TeaserResponseDTO> teasers = List.of(aTeaserResponse());
        when(teaserService.getByGameId(1)).thenReturn(teasers);

        ResponseEntity<List<TeaserResponseDTO>> response = teaserController.getByGameId(1, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(teasers, response.getBody());
        verify(teaserService).getByGameId(1);
        verifyNoMoreInteractions(teaserService);
    }

    @Test
    void getByGameId_whenTypeIsProvided_callsServiceWithType() {
        List<TeaserResponseDTO> teasers = List.of(aTeaserResponse());
        when(teaserService.getByGameIdAndType(1, "image")).thenReturn(teasers);

        ResponseEntity<List<TeaserResponseDTO>> response = teaserController.getByGameId(1, "image");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(teasers, response.getBody());
        verify(teaserService).getByGameIdAndType(1, "image");
        verifyNoMoreInteractions(teaserService);
    }

    @Test
    void getByGameId_whenServiceThrowsException_returnsBadRequest() {
        when(teaserService.getByGameIdAndType(1, "video"))
                .thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<List<TeaserResponseDTO>> response = teaserController.getByGameId(1, "video");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(teaserService).getByGameIdAndType(1, "video");
    }

    private TeaserResponseDTO aTeaserResponse() {
        TeaserResponseDTO dto = new TeaserResponseDTO();
        dto.setId(10);
        dto.setGameId(1);
        dto.setObjectKey("cover.png");
        dto.setType("image");
        return dto;
    }
}
