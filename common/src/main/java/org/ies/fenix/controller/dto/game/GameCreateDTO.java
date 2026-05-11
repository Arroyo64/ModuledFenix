package org.ies.fenix.controller.dto.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameCreateDTO {

    private String title;

    private String description;

    private BigDecimal price;

    private List<String> tags;
}