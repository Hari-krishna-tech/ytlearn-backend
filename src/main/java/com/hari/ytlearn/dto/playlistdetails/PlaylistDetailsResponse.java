package com.hari.ytlearn.dto.playlistdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Top-level response for Playlists: list
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistDetailsResponse {
    private String kind;
    private String etag;
    // pageInfo might be present but less relevant when fetching by ID
    // private PageInfo pageInfo;
    private List<Playlist> items; // List will usually contain only one item when fetching by ID
}