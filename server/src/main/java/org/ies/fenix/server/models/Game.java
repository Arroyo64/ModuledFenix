package org.ies.fenix.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Column(unique = true, nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dev_id")
    private Client dev;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "size_mb")
    private BigDecimal sizeMb;

    @Column(name = "downloads")
    private Integer downloads;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "game_logo_key", length = 255)
    private String gameLogoKey;

    @Column(name = "game_file_key", length = 255)
    private String gameFileKey;

    @ManyToMany
    @JoinTable(
            name = "game_tag",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "game")
    private List<Purchase> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Teaser> teasers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (downloads == null) {
            downloads = 0;
        }

        if (price == null) {
            price = BigDecimal.ZERO;
        }

        if (sizeMb == null) {
            sizeMb = BigDecimal.ZERO;
        }
    }
}