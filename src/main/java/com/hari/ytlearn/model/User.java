package com.hari.ytlearn.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String googleId;

    private String name;
    private String email;

    private String profilePicture;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

}
