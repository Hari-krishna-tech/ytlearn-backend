package com.hari.ytlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String playlistId;
    private String title;
    private String description;
    private int totalVideos;
    private Instant lastSyncedAt;
    private Instant createdAt;
    private Instant updatedAt;

}
