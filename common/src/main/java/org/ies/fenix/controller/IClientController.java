package org.ies.fenix.controller;

import org.ies.fenix.controller.dto.ServerResponseDTO;
import org.ies.fenix.controller.dto.client.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/clients")
public interface IClientController {

    @PostExchange("/register")
    ResponseEntity<RegisterResponseDTO> register(@RequestBody ClientRegisterDTO dto);

    @PostExchange("/login")
    ResponseEntity<LoginResponseDTO> login(@RequestBody ClientLoginDTO dto);

    @PostExchange("/logout")
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization);

    @PostExchange("/username")
    ResponseEntity<ClientNameDTO> getUsername(@RequestHeader("Authorization") String authorization);

    @PostExchange("/bio")
    ResponseEntity<ServerResponseDTO> updateBio(@RequestHeader("Authorization") String authorization, @RequestBody String bio);

    @GetExchange("bio")
    ResponseEntity<String> getBio(@RequestHeader("Authorization") String authorization);

    @DeleteExchange("/pic")
    ResponseEntity<ServerResponseDTO> deleteProfilePicture(@RequestHeader String authorization);

    @PostExchange("/pic")
    ResponseEntity<ServerResponseDTO> uploadProfilePicture(@RequestHeader String authorization, @RequestBody FileUploadDTO dto);

    @GetExchange("/pic")
    ResponseEntity<byte[]> getProfileImage(@RequestHeader String authorization);
}