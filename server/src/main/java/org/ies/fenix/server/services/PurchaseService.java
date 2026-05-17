package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.purchase.LibraryGameDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseCreateDTO;
import org.ies.fenix.controller.dto.purchase.PurchaseResponseDTO;
import org.ies.fenix.server.exception.AlreadyPurchasedException;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.models.Game;
import org.ies.fenix.server.models.Purchase;
import org.ies.fenix.server.repositories.ClientRepository;
import org.ies.fenix.server.repositories.GameRepository;
import org.ies.fenix.server.repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private GameRepository gameRepository;

    public PurchaseResponseDTO createPurchase(PurchaseCreateDTO dto) {
        Client client = clientRepository.findById(dto.getClientId()).orElse(null);
        if (client == null) return null;

        Game game = gameRepository.findById(dto.getGameId()).orElse(null);
        if (game == null) return null;

        if (purchaseRepository.existsByClientIdAndGameId(dto.getClientId(), dto.getGameId())) {
            throw new AlreadyPurchasedException("El cliente ya compró este juego");
        }

        // Registrar compra
        Purchase purchase = new Purchase();
        purchase.setClient(client);
        purchase.setGame(game);
        Purchase saved = purchaseRepository.save(purchase);

        // Incrementar descargas aquí
        game.setDownloads(game.getDownloads() + 1);
        gameRepository.save(game);
        return toResponseDTO(saved);
    }

    public List<LibraryGameDTO> getLibraryByClientId(Integer clientId) {
        List<Purchase> purchases = purchaseRepository.findByClientId(clientId);
        List<LibraryGameDTO> library = new ArrayList<>();

        for (Purchase purchase : purchases) {
            Game game = purchase.getGame();
            LibraryGameDTO dto = new LibraryGameDTO();
            dto.setGameId(game.getId());
            dto.setTitle(game.getTitle());
            library.add(dto);
        }

        return library;
    }

    private PurchaseResponseDTO toResponseDTO(Purchase purchase) {
        PurchaseResponseDTO dto = new PurchaseResponseDTO();
        dto.setId(purchase.getId());
        dto.setClientId(purchase.getClient().getId());
        dto.setGameId(purchase.getGame().getId());
        dto.setGameTitle(purchase.getGame().getTitle());
        dto.setPrice(purchase.getGame().getPrice());
        return dto;
    }

    public boolean hasPurchased(Integer clientId, Integer gameId) {
        return purchaseRepository.existsByClientIdAndGameId(clientId, gameId);
    }

}