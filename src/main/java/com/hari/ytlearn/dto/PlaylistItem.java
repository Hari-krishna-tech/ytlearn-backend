package com.hari.ytlearn.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class PlaylistItem {
    private String kind;
    private String etag;
    private String id;
    private Snippet snippet;
}