package com.hari.ytlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "playlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "youtubePlaylistId"})
})
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private String youtubePlaylistId;
    private String etag;
    @Column(length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private Instant publishedAt;
    private String channelId;
    private String channelTitle;
    private int itemCount;


    // Thumbnail URLs
    private String thumbnailDefaultUrl;
    private String thumbnailMediumUrl;
    private String thumbnailHighUrl;
    private String thumbnailStandardUrl;
    private String thumbnailMaxresUrl;
    private Instant lastSyncedAt;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
