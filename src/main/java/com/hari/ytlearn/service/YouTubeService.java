package com.hari.ytlearn.service;

import com.hari.ytlearn.dto.PlaylistResponse; // For playlist items
import com.hari.ytlearn.dto.playlistdetails.Playlist; // For playlist details
import com.hari.ytlearn.dto.playlistdetails.PlaylistDetailsResponse; // For playlist details response
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
//import reactor.core.publisher.Mono;

@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(
        YouTubeService.class
    );

    private final RestTemplate restTemplate;
    private String apiKey;
    private String baseUrl;

    // Regex to extract playlist ID from various YouTube URL formats
    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile(
        "[&?]list=([a-zA-Z0-9_-]+)"
    );

    public YouTubeService(
            RestTemplate restTemplate,
        @Value("${youtube.api.key}") String apiKey,
        @Value("${youtube.api.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        log.info("YouTubeService initialized with RestTemplate. Base URL: {}", baseUrl);
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_YOUTUBE_API_KEY")) {
            log.warn("YouTube API Key is not configured properly in application.properties!");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("YouTube base URL is not configured properly in application.properties!");
        }
        if (this.restTemplate == null) {
            log.error("RestTemplate injection failed!");
        }
    }

    /**
     * Extracts the YouTube playlist ID from a given URL.
     * @param playlistUrl The full YouTube playlist URL.
     * @return The playlist ID string or null if not found.
     */
    public String extractPlaylistIdFromUrl(String playlistUrl) {
        if (playlistUrl == null || playlistUrl.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = PLAYLIST_ID_PATTERN.matcher(playlistUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.warn("Could not extract playlist ID from URL: {}", playlistUrl);
        return null; // Or throw an exception if preferred
    }

    /**
     * Fetches details about a specific playlist (title, description, item count, etc.).
     * @param playlistUrl The URL of the YouTube playlist.
     * @return A Mono emitting the Playlist details, or Mono.empty() if not found,
     *         or Mono.error() for API/network issues.
     */
    /**
     * Fetches details about a specific playlist (synchronous).
     * @param playlistUrl The URL of the YouTube playlist.
     * @return The Playlist details, or null if not found or an error occurs.
     * @throws IllegalArgumentException if URL is invalid.
     * @throws RestClientException for API/network issues.
     */
    public Playlist getPlaylistDetails(String playlistUrl) {
        String playlistId = extractPlaylistIdFromUrl(playlistUrl);
        if (playlistId == null || playlistId.isBlank()) {
            log.error("Invalid or missing playlist ID in URL: {}", playlistUrl);
            throw new IllegalArgumentException(
                    "Invalid or missing playlist ID in URL."
            );
        }

        log.info("Fetching details for playlist ID: {}", playlistId);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(this.baseUrl) // Start with the base URL
                .path("/playlists")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet,contentDetails")
                .queryParam("id", playlistId);

        URI uri = uriBuilder.build().toUri();
        log.debug("Requesting URI: {}", uri.toString());

        try {
            // Use getForEntity to get status code and body
            ResponseEntity<PlaylistDetailsResponse> responseEntity =
                    this.restTemplate.getForEntity(
                            uri,
                            PlaylistDetailsResponse.class
                    );

            if (
                    responseEntity.getStatusCode() == HttpStatus.OK &&
                            responseEntity.getBody() != null &&
                            responseEntity.getBody().getItems() != null &&
                            !responseEntity.getBody().getItems().isEmpty()
            ) {
                Playlist playlist = responseEntity.getBody().getItems().get(0);
                log.info(
                        "Successfully fetched details for playlist ID: {} - Title: '{}'",
                        playlistId,
                        playlist.getSnippet() != null ? playlist.getSnippet().getTitle() : "N/A"
                );
                return playlist;
            } else {
                // Handle cases where API returns 200 OK but empty list (e.g., playlist deleted)
                log.warn(
                        "Playlist not found for ID: {} (API returned status {} with empty or invalid body)",
                        playlistId,
                        responseEntity.getStatusCode()
                );
                return null; // Indicate not found
            }
        } catch (HttpClientErrorException e) {
            // Handle specific HTTP client errors (4xx)
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Playlist not found for ID: {} (API returned 404)", playlistId);
                return null; // Explicitly return null for 404
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error(
                        "API Key error (Forbidden 403) fetching details for {}: {}. Check quota or key restrictions.",
                        playlistId, e.getResponseBodyAsString(), e);
                // Re-throw or wrap in a custom exception
                throw new RestClientException("YouTube API Forbidden (Check API Key/Quota): " + e.getMessage(), e);
            } else {
                log.error(
                        "HTTP Client Error {} fetching details for {}: {}",
                        e.getStatusCode(), playlistId, e.getResponseBodyAsString(), e);
                // Re-throw or wrap
                throw e;
            }
        } catch (RestClientException e) {
            // Handle other RestTemplate errors (network, server 5xx, deserialization)
            log.error(
                    "Error fetching playlist details for ID {}: {}",
                    playlistId,
                    e.getMessage(),
                    e
            );
            // Re-throw the generic exception
            throw e;
        }
    }


    // --- Method to get playlist items (Synchronous Version) ---
    // If you need this, you'd adapt it similarly using RestTemplate
    public PlaylistResponse getPlaylistItems(String playlistUrl, int maxResults, String pageToken) {
        String playlistId = extractPlaylistIdFromUrl(playlistUrl);
        // ... validation ...

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromHttpUrl(this.baseUrl)
            .path("/playlistItems")
            .queryParam("key", apiKey)
            .queryParam("part", "snippet")
            .queryParam("playlistId", playlistId)
            .queryParam("maxResults", Math.min(maxResults, 50));

        if (pageToken != null && !pageToken.isBlank()) {
            uriBuilder.queryParam("pageToken", pageToken);
        }
        URI uri = uriBuilder.build().toUri();
        log.debug("Requesting URI: {}", uri.toString());

        try {
             ResponseEntity<PlaylistResponse> responseEntity = this.restTemplate.getForEntity(uri, PlaylistResponse.class);
             if (responseEntity.getStatusCode() == HttpStatus.OK) {
                 log.info("Successfully fetched items for playlist ID: {}", playlistId);
                 return responseEntity.getBody();
             } else {
                 log.warn("Received status {} when fetching items for playlist ID: {}", responseEntity.getStatusCode(), playlistId);
                 return null; // Or handle differently
             }
        } catch (HttpClientErrorException e) {
             // ... specific error handling (403, 404 etc.) ...
             log.error("HTTP Client Error {} fetching items for {}: {}", e.getStatusCode(), playlistId, e.getResponseBodyAsString(), e);
             throw e;
        } catch (RestClientException e) {
             log.error("Error fetching playlist items for ID {}: {}", playlistId, e.getMessage(), e);
             throw e;
        }
    }

 /*   // --- Method to get playlist items (from previous example) ---
    public Mono<PlaylistResponse> getPlaylistItems(
        String playlistId,
        int maxResults,
        String pageToken
    ) {
        // ... (implementation from previous answer remains here) ...
        if (playlistId == null || playlistId.isBlank()) {
            return Mono.error(
                new IllegalArgumentException("Playlist ID cannot be blank.")
            );
        }
        log.info(
            "Fetching playlist items for ID: {}, maxResults: {}, pageToken: {}",
            playlistId,
            maxResults,
            pageToken
        );

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromPath("/playlistItems")
            .queryParam("key", apiKey)
            .queryParam("part", "snippet") // Request snippet details
            .queryParam("playlistId", playlistId)
            .queryParam("maxResults", Math.min(maxResults, 50)); // Max 50 per page allowed by API

        if (pageToken != null && !pageToken.isBlank()) {
            uriBuilder.queryParam("pageToken", pageToken);
        }

        URI uri = uriBuilder.build().toUri();
        log.debug("Requesting URI: {}", baseUrl + uri.toString()); // Log full URI for debugging

        return this.webClient.get()
            .uri(uri)
            .retrieve()
            .onStatus(
                // Handle specific API errors
                status -> status.isSameCodeAs(HttpStatus.FORBIDDEN),
                clientResponse -> clientResponse
                    .bodyToMono(String.class) // Read body for details
                    .flatMap(body -> {
                        log.error(
                            "API Key error (Forbidden): {}. Check quota or key restrictions.",
                            body
                        );
                        return Mono.error(
                            new RuntimeException(
                                "YouTube API Forbidden (Check API Key/Quota): " +
                                body
                            )
                        );
                    })
            )
            .onStatus(
                // Handle other client/server errors
                status -> status.is4xxClientError() || status.is5xxServerError(),
                clientResponse -> clientResponse
                    .bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error(
                            "YouTube API Error ({}): {}",
                            clientResponse.statusCode(),
                            body
                        );
                        return Mono.error(
                            new RuntimeException(
                                "YouTube API Error: " +
                                clientResponse.statusCode() +
                                " " +
                                body
                            )
                        );
                    })
            )
            .bodyToMono(PlaylistResponse.class) // Deserialize successful response
            .doOnSuccess(response ->
                log.info(
                    "Successfully fetched {} items for playlist ID: {}. NextPageToken: {}",
                    response.getItems() != null ? response.getItems().size() : 0,
                    playlistId,
                    response.getNextPageToken()
                )
            )
            .doOnError(error ->
                log.error(
                    "Error fetching playlist items for ID: {}",
                    playlistId,
                    error
                )
            );
    } */
}
