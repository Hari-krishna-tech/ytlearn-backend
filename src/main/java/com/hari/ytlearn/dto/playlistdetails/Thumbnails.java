package com.hari.ytlearn.dto.playlistdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// Reusable Thumbnail classes (can be shared or kept separate)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Thumbnails {
    private Thumbnail defaultThumbnail; // Renamed field
    private Thumbnail medium;
    private Thumbnail high;
    private Thumbnail standard;
    private Thumbnail maxres;
}