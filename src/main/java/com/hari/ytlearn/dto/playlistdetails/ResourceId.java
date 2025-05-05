package com.hari.ytlearn.dto.playlistdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contains the video ID
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ResourceId {
    private String kind;
    private String videoId;
}