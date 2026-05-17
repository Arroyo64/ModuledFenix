package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.ServerResponseDTO;
import org.ies.fenix.controller.dto.client.*;
import org.ies.fenix.server.services.ClientService;
import org.ies.fenix.server.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.ies.fenix.controller.IClientController;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.ies.fenix.server.services.TokenService.extractBearerToken;

@RestController
public class ClientController implements IClientController {

    @Autowired
    private ClientService clientService;

    @Override
    public ResponseEntity<RegisterResponseDTO> register(ClientRegisterDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(clientService.register(dto));
    }

    @Override
    public ResponseEntity<LoginResponseDTO> login(ClientLoginDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(clientService.login(dto));
    }

    @Override
    public ResponseEntity<Void> logout(String authorization) {
        String token = extractBearerToken(authorization);
        if (token != null) {
            clientService.logout(token);
        }
        return ResponseEntity.ok().build();
    }


    @Override
    public ResponseEntity<ClientInfoDTO> getClientInfo(String authorization) {
        String token = extractBearerToken(authorization);
        if (token != null) {
            System.out.println("Recibido token = " + token);
            var client = clientService.getClient(token);

            return ResponseEntity.ok(
                    new ClientInfoDTO(
                            client.getUsername(),
                            client.getEmail(),
                            client.getCharacterCounterPassword() + 1
                    )
            );
        }
        return ResponseEntity.badRequest().build();
    }
    @Override
    public ResponseEntity<ServerResponseDTO> updateBio(String authorization, String bio){
        String token = extractBearerToken(authorization);
        if (token != null) {
            return ResponseEntity.ok(clientService.updateBio(token, bio));
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<String> getBio(String authorization) {
        String token = extractBearerToken(authorization);
        if (token != null) {
            return ResponseEntity.ok(clientService.getClient(token).getBio());
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<ServerResponseDTO> deleteProfilePicture(String authorization){
        String token = extractBearerToken(authorization);
        if (token != null) {
            return ResponseEntity.ok(clientService.deleteImageProfile(token));
        }
        return ResponseEntity.badRequest().build();
    }
    @Override
    public ResponseEntity<ServerResponseDTO> uploadProfilePicture(
            @RequestHeader("Authorization") String authorization,
            @RequestBody FileUploadDTO dto
    ) {
        ServerResponseDTO result = clientService.uploadImageProfile(dto, extractBearerToken(authorization));

        if ("ERROR".equals(result.getStatus())) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<byte[]> getProfileImage(
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            byte[] bytes = clientService.getProfilePicture(extractBearerToken(authorization));

            if (bytes.length == 0) {
                return ResponseEntity.status(404).build();
            }

            String contentType = FileUtils.getContentType(bytes, "profile");

            return ResponseEntity
                    .ok()
                    .header("Content-Type", contentType)
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}