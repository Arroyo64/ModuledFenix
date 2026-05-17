package org.ies.fenix.server.controller;

import org.ies.fenix.controller.ITagController;
import org.ies.fenix.controller.dto.tag.TagResponseDTO;
import org.ies.fenix.server.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TagController implements ITagController {

    @Autowired
    private TagService tagService;

    @Override
    public ResponseEntity<List<TagResponseDTO>> getAll() {
        try {
            return ResponseEntity.ok(tagService.getAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<TagResponseDTO> getById(Integer id) {
        try {
            TagResponseDTO response = tagService.getById(id);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}