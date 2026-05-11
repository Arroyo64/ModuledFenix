package org.ies.fenix.controller.dto.teaser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeaserResponseDTO {

    private Integer id;

    private Integer gameId;

    private String objectKey;

    private String type;
}