package org.ies.fenix.server.controller;


import org.ies.fenix.controller.ITeaserController;
import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;
import org.ies.fenix.server.services.TeaserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
public class TeaserController implements ITeaserController {

    @Autowired
    private TeaserService teaserService;

    @Override
    public ResponseEntity<List<TeaserResponseDTO>> getByGameId(Integer gameId, String type) {
        try {
            if (type != null) {
                return ResponseEntity.ok(teaserService.getByGameIdAndType(gameId, type));
            }

            return ResponseEntity.ok(teaserService.getByGameId(gameId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}