package org.ies.fenix.controller.dto.purchase;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponseDTO { //para crear una factura asi como hicimos con Diego-San
    private Integer id;
    private Integer clientId;
    private Integer gameId;
    private String gameTitle;
    private BigDecimal price;
}