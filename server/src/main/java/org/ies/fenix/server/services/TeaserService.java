package org.ies.fenix.server.services;

import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;
import org.ies.fenix.server.models.Teaser;
import org.ies.fenix.server.repositories.TeaserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeaserService {

    @Autowired
    private TeaserRepository teaserRepository;

    public List<TeaserResponseDTO> getByGameId(Integer gameId) {
        List<Teaser> teasers = teaserRepository.findByGameId(gameId);
        List<TeaserResponseDTO> response = new ArrayList<>();

        for (Teaser teaser : teasers) {
            response.add(toResponseDTO(teaser));
        }

        return response;
    }

    public List<TeaserResponseDTO> getByGameIdAndType(Integer gameId, String type) {
        List<Teaser> teasers = teaserRepository.findByGameIdAndType(gameId, type);
        List<TeaserResponseDTO> response = new ArrayList<>();

        for (Teaser teaser : teasers) {
            response.add(toResponseDTO(teaser));
        }

        return response;
    }

    private TeaserResponseDTO toResponseDTO(Teaser teaser) {
        TeaserResponseDTO dto = new TeaserResponseDTO();
        dto.setId(teaser.getId());
        dto.setGameId(teaser.getGame().getId());
        dto.setFileName(teaser.getObjectKey());
        dto.setType(teaser.getType());
        return dto;
    }
}