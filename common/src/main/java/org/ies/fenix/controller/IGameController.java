package org.ies.fenix.controller;

import org.ies.fenix.controller.dto.game.GameResponseDTO;
import org.ies.fenix.controller.dto.game.GameSearchDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/api/games")
public interface IGameController {

    @GetExchange
    ResponseEntity<List<GameResponseDTO>> getAllGames();

    @PostExchange("/search")
    ResponseEntity<List<GameResponseDTO>> getManyGames(
            @RequestBody GameSearchDTO dto
    );

    @GetExchange("/{id}")
    ResponseEntity<GameResponseDTO> getById(
            @PathVariable Integer id
    );
}