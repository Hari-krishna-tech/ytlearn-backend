package com.hari.ytlearn.dto;


import com.hari.ytlearn.model.Note;
import com.hari.ytlearn.model.Playlist;
import com.hari.ytlearn.model.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long id;

    private String youtubePlaylistItemId;

    private String youtubeVideoId;

    private String title;

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

    private Status status;
    private Note note;

    private Instant createdAt;
    private Instant updatedAt;
}
