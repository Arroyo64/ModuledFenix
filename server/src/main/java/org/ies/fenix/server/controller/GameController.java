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

    @Override
    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        try {
            return ResponseEntity.ok(gameService.getAllGames());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

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

    @Override
    @GetMapping("/api/games/{id}/logo")
    public ResponseEntity<byte[]> getLogo(String authorization, Integer id) {
        byte[] bytes = gameService.loadLogo(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    @Override
    @GetMapping("/api/games/{id}/vertical")
    public ResponseEntity<byte[]> getVertical(String authorization, Integer id) {
        byte[] bytes = gameService.getVerticalImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    @Override
    @GetMapping("/api/games/{id}/horizontal1")
    public ResponseEntity<byte[]> getHorizontal1(String authorization, Integer id) {
        byte[] bytes = gameService.getHorizontal1Image(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }
    @Override
    @GetMapping("/api/games/{id}/horizontal2")
    public ResponseEntity<byte[]> getHorizontal2(String authorization, Integer id) {
        byte[] bytes = gameService.getHorizontal2Image(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getById(@PathVariable Integer id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(gameService.getGameById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(
            value = "/create/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<GameResponseDTO> uploadGame(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam("gameFile") MultipartFile gameFile,
            @RequestParam("logoFile") MultipartFile logoFile,
            @RequestParam(value = "verticalImage", required = false) MultipartFile verticalImage,
            @RequestParam(value = "horizontalImageOne", required = false) MultipartFile horizontalImageOne,
            @RequestParam(value = "horizontalImageTwo", required = false) MultipartFile horizontalImageTwo
    ) {
        try {
            GameResponseDTO response = gameService.createGame(
                    authorization,
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

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadGame(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    ) {
        try {
            Resource file = gameService.downloadGame(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}