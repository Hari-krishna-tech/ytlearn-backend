package com.hari.ytlearn.controller;

import com.hari.ytlearn.dto.PlaylistResponse;
import com.hari.ytlearn.dto.playlistdetails.Playlist;
// Import DTO for playlist items if you keep that functionality
// import com.example.youtubequery.dto.PlaylistResponse;
import com.hari.ytlearn.service.YouTubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private static final Logger log = LoggerFactory.getLogger(
            YouTubeController.class
    );
    private final YouTubeService youTubeService;

    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Endpoint to get details about a specific playlist (Synchronous).
     * @param url The full URL of the YouTube playlist.
     * @return ResponseEntity containing the Playlist details or an error status.
     */
    @GetMapping("/playlist/details")
    public ResponseEntity<Playlist> getPlaylistDetailsByUrl(
            @RequestParam String url
    ) {
        log.info("Received request for playlist details URL: {}", url);
        try {
            Playlist playlistDetails = youTubeService.getPlaylistDetails(url);
            if (playlistDetails != null) {
                return ResponseEntity.ok(playlistDetails);
            } else {
                // Service returned null, likely means 404 Not Found from API
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Bad request for playlist details: {}", e.getMessage());
            // Return 400 Bad Request for invalid URL format
            // Consider returning an error DTO instead of null body
            return ResponseEntity.badRequest().body(null);
        } catch (RestClientException e) {
            // Catch exceptions thrown by the service for API/network errors
            log.error(
                    "API Error during playlist details fetch: {}",
                    e.getMessage(),
                    e
            );
            // Return 500 Internal Server Error (or more specific if possible)
            // Consider returning an error DTO
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception e) {
            // Catch any other unexpected errors
            log.error("Unexpected error fetching playlist details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- Endpoint for playlist items (Synchronous Version) ---
    @GetMapping("/playlist/items")
    public ResponseEntity<PlaylistResponse> getPlaylistItemsByUrl(
        @RequestParam String url,
        @RequestParam(defaultValue = "100") int maxResults,
        @RequestParam(required = false) String pageToken
    ) {
         log.info(
            "Received request for playlist items URL: {}, maxResults: {}, pageToken: {}",
            url, maxResults, pageToken);
        try {
            PlaylistResponse response = youTubeService.getPlaylistItems(url, maxResults, pageToken);
             if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                // Decide how to handle null response (e.g., 404 or 500 depending on service logic)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } catch (IllegalArgumentException e) {
            log.error("Bad request for playlist items: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RestClientException e) {
            log.error("API Error during playlist items fetch: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
             log.error("Unexpected error fetching playlist items: {}", e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
