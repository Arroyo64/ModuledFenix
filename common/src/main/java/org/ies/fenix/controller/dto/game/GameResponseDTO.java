package org.ies.fenix.controller.dto.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ies.fenix.controller.dto.teaser.TeaserResponseDTO;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameResponseDTO {

    private Integer id;

    private String title;

    private String description;

    private String sizeApproximation;

    private String downloadsApproximation;

    private BigDecimal price;

    private String devUsername;

    private String gameLogoKey;

    private String gameFileKey;

    private List<String> tags;

    private List<TeaserResponseDTO> teasers;

    public GameResponseDTO(Integer id, String title, String description, BigDecimal sizeMb, BigDecimal price, Integer downloads) {
    }
}