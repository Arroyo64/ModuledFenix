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

    private static final String BASE_UPLOAD_DIR_PROFILE =
            Paths.get("").toAbsolutePath() + "/uploads/profile/";

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
                    .message("Username does not exist")
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
        if (token == null || token.isBlank()) {
            return false;
        }

        String cleanToken = token.trim();

        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }

        tokenService.revoke(cleanToken);
        return true;
    }
    public Client getClient(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String cleanToken = token.trim();

        if (cleanToken.startsWith("Bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }

        if (!tokenService.isValid(cleanToken)) {
            return null;
        }

        return clientRepository.findByAuthTokensToken(cleanToken);
    }
    public ServerResponseDTO uploadImageProfile(FileUploadDTO dto, String token) {
        Client client = getClient(token);
        if (client == null) return new ServerResponseDTO("ERROR", "Token is not valid");
        if (dto.getBytes() == null) return new ServerResponseDTO("ERROR", "No image provided");

        try {
            String detectedType = FileUtils.getContentType(dto.getBytes(), dto.getFileName());
            if (!detectedType.startsWith("image/")) {
                return new ServerResponseDTO("ERROR", "File type not supported");
            }

            if (!detectedType.equals(dto.getFileType())) {
                return new ServerResponseDTO("ERROR", "Invalid file type");
            }

            String key = client.getProfileImageKey();
            if (key == null) {
                key = UUID.randomUUID().toString().replace("-", "");
            }
            final String finalKey = key;

            // === RUTA PROFESIONAL USANDO VARIABLE ===
            String folderPath = Paths.get(BASE_UPLOAD_DIR_PROFILE, String.valueOf(client.getId()))
                    .toAbsolutePath()
                    .normalize()
                    .toString();

            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            // Borrar anteriores
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(finalKey + ".")) {
                        f.delete();
                    }
                }
            }

            String ext = FileUtils.getExtension(dto.getFileName());
            if (ext.isEmpty()) ext = "img";

            String filePath = folderPath + File.separator + finalKey + "." + ext;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(dto.getBytes());
            }

            client.setProfileImageKey(finalKey);
            clientRepository.save(client);

            return new ServerResponseDTO("OK", "Image uploaded successfully");

        } catch (IOException e) {
            return new ServerResponseDTO("ERROR", "Error saving image");
        }
    }
    public byte[] getProfilePicture(String authorization) {
        Client client = getClient(authorization);
        if (client == null) return new byte[0];

        String key = client.getProfileImageKey();
        if (key == null) return new byte[0];

        // === RUTA PROFESIONAL ===
        String folderPath = Paths.get(BASE_UPLOAD_DIR_PROFILE, String.valueOf(client.getId()))
                .toAbsolutePath()
                .normalize()
                .toString();

        File folder = new File(folderPath);
        if (!folder.exists()) return new byte[0];

        File[] files = folder.listFiles();
        if (files == null) return new byte[0];

        for (File f : files) {
            if (f.getName().startsWith(key + ".")) {
                try {
                    return Files.readAllBytes(f.toPath());
                } catch (IOException ignored) {}
            }
        }

        return new byte[0];
    }

    public ServerResponseDTO deleteImageProfile(String token) {
        Client client = getClient(token);
        if (client == null) return new ServerResponseDTO("ERROR", "Token is not valid");

        String key = client.getProfileImageKey();
        if (key == null) return new ServerResponseDTO("ERROR", "No profile image to delete");

        // === RUTA PROFESIONAL ===
        String folderPath = Paths.get(BASE_UPLOAD_DIR_PROFILE, String.valueOf(client.getId()))
                .toAbsolutePath()
                .normalize()
                .toString();

        File dir = new File(folderPath);
        File[] matches = dir.listFiles((d, name) -> name.startsWith(key + "."));

        if (matches != null && matches.length > 0) {
            matches[0].delete();
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

}