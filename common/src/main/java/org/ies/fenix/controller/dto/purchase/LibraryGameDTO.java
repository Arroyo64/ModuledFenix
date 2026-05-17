package org.ies.fenix.controller.dto.purchase;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LibraryGameDTO {
    //Para filtrar los games que se cargen
    private Integer gameId;
    //Cargar el name de los game en el fxml
    private String title;
}