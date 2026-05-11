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

    @Column(name = "size_mb")
    private BigDecimal sizeMb;

    @Column(name = "downloads")
    private Integer downloads;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "game_logo_key")
    private String gameLogoKey;

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