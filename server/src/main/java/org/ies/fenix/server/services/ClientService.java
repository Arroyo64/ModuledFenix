package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.ServerResponseDTO;
import org.ies.fenix.controller.dto.client.*;
import org.ies.fenix.server.models.Client;
import org.ies.fenix.server.repositories.ClientRepository;
import org.ies.fenix.server.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.io.FilenameUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    public RegisterResponseDTO register(ClientRegisterDTO dto) {
        if (clientRepository.findByUsername(dto.getUsername()).isPresent()) {
            return aResponseRegister("This username already exists", "WARN", false);
        }
        if (clientRepository.findByEmail(dto.getEmail()).isPresent()) {
            return aResponseRegister("This email is already linked to an account", "WARN", false);
        }
        if (dto.getEmail().isEmpty()) {
            return aResponseRegister("No email attached", "ERROR", false);
        }
        if (dto.getUsername().isBlank()) {
            return aResponseRegister("No username attached", "ERROR", false);
        }

        Client client = new Client();
        client.setUsername(dto.getUsername());
        client.setEmail(dto.getEmail());
        client.setPasswordHashed(passwordEncoder.encode(dto.getPassword()));
        client.setCharacterCounterPassword(dto.getPassword().length()-1);
        client.setBio(null);
        clientRepository.save(client);

        return aResponseRegister("User registered successfully", "OK", true);
    }

    private RegisterResponseDTO aResponseRegister(String message, String status, boolean access) {
        return RegisterResponseDTO.builder()
                .status(status)
                .message(message)
                .access(access)
                .build();
    }

    public LoginResponseDTO login(ClientLoginDTO dto) {
        Optional<Client> clientOpt = clientRepository.findByUsername(dto.getUsername());

        if (clientOpt.isEmpty()) {
            return LoginResponseDTO.builder()
                    .status("WARN")
                    .message("Password incorrect")
                    .build();
        }

        Client client = clientOpt.get();

        if (!passwordEncoder.matches(dto.getPassword(), client.getPasswordHashed())) {
            return LoginResponseDTO.builder()
                    .status("WARN")
                    .message("Password incorrect")
                    .build();
        }

        String token = tokenService.generateToken(client.getId());

        return LoginResponseDTO.builder()
                .status("OK")
                .message("Login successful")
                .clientId(client.getId())
                .username(client.getUsername())
                .token(token)
                .build();
    }

    public boolean logout(String token) {
        tokenService.revoke(token);
        return true;
    }
    public Client getClient(String token){
        if (tokenService.isValid(token))
            return clientRepository.findByAuthTokensToken(token);
        else
            return null;
    } //needs to develop the null return statement
    public ServerResponseDTO uploadImageProfile(FileUploadDTO dto, String token) {
        Client client = getClient(token);
        if (client == null) {
            return new ServerResponseDTO("ERROR", "Token is not valid");
        }
        if (dto.getBytes() == null) {
            return new ServerResponseDTO("ERROR", "No image provided");
        }
        try {
            String contentType = FileUtils.getContentType(dto.getBytes(), dto.getFileName());
            if (!contentType.startsWith("image/")) {
                return new ServerResponseDTO("ERROR", "File type not supported");
            }
            if (!contentType.equals(dto.getFileType())) {
                return new ServerResponseDTO("ERROR", "Invalid file type");
            }
            String key = client.getProfileImageKey();
            if (key == null) {
                key = UUID.randomUUID().toString().replace("-", "");
            }
            String folder = "uploads/profile/" + client.getId();
            new File(folder).mkdirs();
            String extension = "." + FilenameUtils.getExtension(dto.getFileName());
            String path = folder + "/" + key + extension;
            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write(dto.getBytes());
            }
            client.setProfileImageKey(key);
            clientRepository.save(client);
            return new ServerResponseDTO("OK", "Image uploaded successfully");
        } catch (IOException e) {
            return new ServerResponseDTO("ERROR", "Error getting content type");
        }
    }
    public ServerResponseDTO deleteImageProfile(String token) {
        Client client = getClient(token);
        if (client == null) {
            return new ServerResponseDTO("ERROR", "Token is not valid");
        }
        String key = client.getProfileImageKey();
        if (key == null) {
            return new ServerResponseDTO("ERROR", "No profile image to delete");
        }
        String folder = "uploads/profile/" + client.getId();
        File dir = new File(folder);
        File[] matches = dir.listFiles((d, name) -> name.startsWith(key + "."));
        if (matches != null && matches.length > 0) {
            File file = matches[0];
            if (file.delete()) {
                System.out.println("Deleted image: " + file.getAbsolutePath());
            } else {
                System.err.println("Failure deleting image: " + file.getAbsolutePath());
            }
        }
        client.setProfileImageKey(null);
        clientRepository.save(client);
        return new ServerResponseDTO("OK", "Image deleted successfully");
    }
    public ServerResponseDTO updateBio(String token, String bio) {
        Client client = getClient(token);
        if (client == null) {
            return new ServerResponseDTO("ERROR", "Token is not valid");
        }
        client.setBio(bio);
        clientRepository.save(client);
        return new ServerResponseDTO("OK", "Bio updated successfully");
    }

    public byte[] getProfilePicture(String authorization) {
        Client client = getClient(authorization);
        if (client == null) {
            return new byte[0];
        }
        String key = client.getProfileImageKey();
        if (key == null) {
            return new byte[0];
        }
        Path folder = Paths.get("uploads", "profile", String.valueOf(client.getId()));
        File dir = folder.toFile();

        File[] files = dir.listFiles((d, name) -> name.startsWith(key + "."));
        if (files == null || files.length == 0) {
            return new byte[0];
        }

        try {
            return Files.readAllBytes(files[0].toPath());
        } catch (IOException e) {
            // log.error(...)
            return new byte[0];
        }
    }
}