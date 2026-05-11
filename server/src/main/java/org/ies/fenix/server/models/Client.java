package org.ies.fenix.server.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    @Column(name = "password_hashed")
    private String passwordHashed;

    @Column(unique = true)
    private String username;

    @Column(length = 250)
    private String bio;

    @Column(name = "profile_image_key", length = 255)
    private String profileImageKey;

    @OneToMany(mappedBy = "user")
    private List<AuthToken> authTokens;

    @OneToMany(mappedBy = "dev")
    private List<Game> developedGames;

    @OneToMany(mappedBy = "client")
    private List<Purchase> purchases;

    @Column(name = "character_counter_password")
    private Integer characterCounterPassword;
}