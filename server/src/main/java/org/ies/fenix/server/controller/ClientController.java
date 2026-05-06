package org.ies.fenix.server.controller;

import org.ies.fenix.controller.dto.ServerResponseDTO;
import org.ies.fenix.controller.dto.client.*;
import org.ies.fenix.server.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.ies.fenix.controller.IClientController;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientController implements IClientController {

    @Autowired
    private ClientService clientService;

    public ResponseEntity<RegisterResponseDTO> register(ClientRegisterDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(clientService.register(dto));
    }


    public ResponseEntity<LoginResponseDTO> login(ClientLoginDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(clientService.login(dto));
    }

    public ResponseEntity<Void> logout(String authorization) {
        String token = extractBearerToken(authorization);
        if (token != null) {
            clientService.logout(token);
        }
        return ResponseEntity.ok().build();
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    @Override
    public ResponseEntity<ClientNameDTO> getUsername(String authorization) {
        String token = extractBearerToken(authorization);
        if (token != null) {
            System.out.println("Recibido token = " + token);
            return ResponseEntity.ok( new ClientNameDTO(clientService.getClient(token).getUsername()));
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
    public ResponseEntity<ServerResponseDTO> uploadProfilePicture(String authorization, FileUploadDTO dto){
        String token = extractBearerToken(authorization);
        if (token != null) {
            return ResponseEntity.ok(clientService.uploadImageProfile(dto, token));
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<byte[]> getProfileImage(String authorization) {
        byte[] bytes = clientService.getProfilePicture(extractBearerToken(authorization));

        // Si no hay imagen → devolver 200 con array vacío
        if (bytes.length == 0) {
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/octet-stream")
                    .body(new byte[0]);
        }

        // Si hay imagen → devolverla con el tipo correcto
        return ResponseEntity
                .ok()
                .header("Content-Type", "image/png") // o dinámico si quieres
                .body(bytes);
    }

}