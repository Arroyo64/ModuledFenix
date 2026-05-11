package org.ies.fenix.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 50)
    private String title;

    @ManyToOne
    @JoinColumn(name = "dev_id")
    private Client dev;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tamano_mb")
    private BigDecimal tamanoMb;

    @Column(name = "downloads")
    private Integer downloads;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "game_file_key")
    private String gameFileKey;

    @Column(name = "logo_image_key")
    private String logoImageKey;

    @Column(name = "vertical_image_key")
    private String verticalImageKey;

    @Column(name = "horizontal_image_one_key")
    private String horizontalImageOneKey;

    @Column(name = "horizontal_image_two_key")
    private String horizontalImageTwoKey;

    @ManyToMany
    @JoinTable(
            name = "game_tag",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "game")
    private List<Purchase> purchases;

    @OneToMany(mappedBy = "game")
    private List<Teaser> teasers;
}