package org.ies.fenix.server.controller;

import org.ies.fenix.controller.IGameController;
import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.ies.fenix.server.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController implements IGameController {

    @Autowired
    private GameService gameService;

    // ============================================================
    //                      GET ALL GAMES
    // ============================================================

    @Override
    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames(
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            return ResponseEntity.ok(gameService.getAllGames());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    //                      SEARCH GAMES
    // ============================================================

    @Override
    @PostMapping("/search")
    public ResponseEntity<List<GameResponseDTO>> getManyGames(@RequestBody GameSearchDTO dto) {
        try {
            if (dto == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(gameService.getGames(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


    // ============================================================
    //                      GET IMAGES
    // ============================================================

    @Override
    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getLogo(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(gameService.loadLogo(id));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes());
        }
    }

    @Override
    @GetMapping("/{id}/vertical")
    public ResponseEntity<byte[]> getVertical(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(gameService.getVerticalImage(id));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes());
        }
    }

    @Override
    @GetMapping("/{id}/horizontal1")
    public ResponseEntity<byte[]> getHorizontal1(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(gameService.getHorizontal1Image(id));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes());
        }
    }

    @Override
    @GetMapping("/{id}/horizontal2")
    public ResponseEntity<byte[]> getHorizontal2(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(gameService.getHorizontal2Image(id));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage().getBytes());
        }
    }


    // ============================================================
    //                      GET GAME BY ID
    // ============================================================

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id) {
        try {
            return ResponseEntity.ok(gameService.getGameById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    // ============================================================
    //                      UPLOAD GAME
    // ============================================================

    @PostMapping(
            value = "/create/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadGame(
            @RequestHeader("Authorization") String token,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("tags") String tags,
            @RequestParam("gameFile") MultipartFile gameFile,
            @RequestParam("logoFile") MultipartFile logoFile,
            @RequestParam(value = "verticalImage", required = false) MultipartFile verticalImage,
            @RequestParam(value = "horizontalImageOne", required = false) MultipartFile horizontalImageOne,
            @RequestParam(value = "horizontalImageTwo", required = false) MultipartFile horizontalImageTwo
    ) {

        try {
            GameResponseDTO dto = gameService.createGame(
                    token,
                    title,
                    description,
                    price,
                    tags,
                    gameFile,
                    logoFile,
                    verticalImage,
                    horizontalImageOne,
                    horizontalImageTwo
            );

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ============================================================
    //                      DOWNLOAD GAME
    // ============================================================

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadGame(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    ) {
        try {
            Resource file = gameService.downloadGame(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
