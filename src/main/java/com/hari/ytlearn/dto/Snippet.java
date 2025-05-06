package com.hari.ytlearn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contains details about the video
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Snippet {
    private String publishedAt;
    private String channelId;
    private String title;
    private String description;
    private Thumbnails thumbnails;
    private String channelTitle;
    private String playlistId;
    private int position;
    private ResourceId resourceId;
    private String videoOwnerChannelTitle;
    private String videoOwnerChannelId;
}