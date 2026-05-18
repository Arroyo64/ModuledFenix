package org.ies.fenix.controller;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/api/games")
public interface IGameController {

    @GetExchange
    ResponseEntity<List<GameResponseDTO>> getAllGames(
            @RequestHeader("Authorization") String authorization
    );

    @PostExchange("/search")
    ResponseEntity<List<GameResponseDTO>> getManyGames(
            @RequestBody GameSearchDTO dto
    );

    @GetExchange("/{id}")
    ResponseEntity<GameResponseDTO> getById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    );

    @GetExchange("/{id}/vertical")
    ResponseEntity<byte[]> getVertical(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    );

    @GetExchange("/{id}/horizontal1")
    ResponseEntity<byte[]> getHorizontal1(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    );

    @GetExchange("/{id}/horizontal2")
    ResponseEntity<byte[]> getHorizontal2(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    );

    @GetExchange("/{id}/logo")
    ResponseEntity<byte[]> getLogo(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer id
    );
}