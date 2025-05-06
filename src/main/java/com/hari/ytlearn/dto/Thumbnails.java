package com.hari.ytlearn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contains thumbnail URLs
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Thumbnails {
    private Thumbnail defaultThumbnail; // Renamed field
    private Thumbnail medium;
    private Thumbnail high;
    // Add standard, maxres if needed
}