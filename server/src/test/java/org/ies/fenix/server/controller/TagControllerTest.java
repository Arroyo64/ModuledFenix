package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.tag.TagResponseDTO;
import org.ies.fenix.server.services.TagService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    @Test
    void getAll_returnsOkWithTags() {
        List<TagResponseDTO> tags = List.of(new TagResponseDTO(1, "fantasy", "Fantasy games"));
        when(tagService.getAll()).thenReturn(tags);

        ResponseEntity<List<TagResponseDTO>> response = tagController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tags, response.getBody());
        verify(tagService).getAll();
    }

    @Test
    void getAll_whenServiceThrowsException_returnsBadRequest() {
        when(tagService.getAll()).thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<List<TagResponseDTO>> response = tagController.getAll();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(tagService).getAll();
    }

    @Test
    void getById_whenTagExists_returnsOkWithTag() {
        TagResponseDTO tag = new TagResponseDTO(1, "fantasy", "Fantasy games");
        when(tagService.getById(1)).thenReturn(tag);

        ResponseEntity<TagResponseDTO> response = tagController.getById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tag, response.getBody());
        verify(tagService).getById(1);
    }

    @Test
    void getById_whenServiceReturnsNull_returnsNotFound() {
        when(tagService.getById(99)).thenReturn(null);

        ResponseEntity<TagResponseDTO> response = tagController.getById(99);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(tagService).getById(99);
    }

    @Test
    void getById_whenServiceThrowsException_returnsBadRequest() {
        when(tagService.getById(1)).thenThrow(new RuntimeException("unexpected error"));

        ResponseEntity<TagResponseDTO> response = tagController.getById(1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(tagService).getById(1);
    }
}
