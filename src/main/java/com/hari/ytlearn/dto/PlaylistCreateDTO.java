package com.hari.ytlearn.dto; // Or your preferred DTO package

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistCreateDTO {

    private String youtubePlaylistId;

    private String title; // Optional, can be fetched from YouTube API

    private String description; // Optional, can be fetched

    private Instant publishedAt; // Optional, can be fetched

    private String channelId; // Optional, can be fetched

    private String channelTitle; // Optional, can be fetched

    private Integer itemCount; // Optional, can be fetched. Use Integer for nullability

    // Thumbnail URLs - these are also likely fetched, but can be included if needed
    private String thumbnailDefaultUrl;

    private String thumbnailMediumUrl;

    private String thumbnailHighUrl;

    private String thumbnailStandardUrl;

    private String thumbnailMaxresUrl;


    private int totalCompletions; // Optional, can be fetched from YouTube API

    // Note: The 'user' field is intentionally omitted as per your request.
    // The user association would typically be handled in the service layer,
    // for example, by taking the authenticated user's ID or a userId as a
    // separate parameter in the service method.
}
