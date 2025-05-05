package com.hari.ytlearn.dto.playlistdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contains pagination info
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class PageInfo {
    private int totalResults;
    private int resultsPerPage;
}