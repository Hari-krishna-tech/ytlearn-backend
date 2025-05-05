package com.hari.ytlearn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// Represents a single thumbnail image
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class Thumbnail {
    private String url;
    private int width;
    private int height;
}