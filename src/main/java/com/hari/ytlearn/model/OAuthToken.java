package com.hari.ytlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "oauth_token")
public class OAuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String accessToken;

    private String refreshToken;

    @Column(name = "token_type")
    private String tokenType;

    private Instant expiresAt;

    private String scope;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

}
