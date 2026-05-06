package org.ies.fenix.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "teaser")
public class Teaser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "game_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_teaser_game")
    )
    private Game game;

    @Column(name = "object_key", nullable = false, length = 255)
    private String objectKey;

    @Column(name = "type", length = 50)
    private String type;

}