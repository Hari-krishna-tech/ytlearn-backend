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
    @JoinColumn(name = "playlist_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Playlist playlist;

    private String youtubeVideoId;

    private String title;
    private String description;

    private String position;

    private Status status;

    private Instant createdAt;
    private Instant updatedAt;



}

