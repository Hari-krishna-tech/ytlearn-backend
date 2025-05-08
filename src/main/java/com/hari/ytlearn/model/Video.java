package com.hari.ytlearn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "videos")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", referencedColumnName = "id" )
    private Playlist playlist;

    @Column(nullable = false)
    private String youtubePlaylistItemId;

    @Column(nullable = false)
    private String youtubeVideoId;

    @Column(length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;


    private Instant publishedAt;
    // Thumbnail URLs
    private String thumbnailDefaultUrl;
    private String thumbnailMediumUrl;
    private String thumbnailHighUrl;
    private String etag;
    // Information about the channel that uploaded the video
    private String videoOwnerChannelId;
    private String videoOwnerChannelTitle;

    private String position;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id", referencedColumnName = "id")
    private Note note;


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

