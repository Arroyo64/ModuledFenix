package org.ies.fenix.controller;


import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/api/purchases")
public interface IPurchaseController {

    @PostExchange
    ResponseEntity<?> createPurchase(
            @RequestHeader String authorization,
            @RequestBody PurchaseCreateDTO dto);

    @GetExchange("/client/{clientId}/library")
    ResponseEntity<List<LibraryGameDTO>> getLibraryByClientId(
            @RequestHeader String authorization,
            @PathVariable Integer clientId
    );

    @GetExchange("/api/purchases/hasPurchased")
    ResponseEntity<Boolean> hasPurchased(
            @RequestHeader String authorization,
            @RequestParam Integer clientId,
            @RequestParam Integer gameId
    );
}