package com.hari.ytlearn.service;

import com.hari.ytlearn.dto.playlistdetails.PlaylistResponse; // For playlist items
import com.hari.ytlearn.dto.playlistdetails.PlaylistItem; // For playlist details
import com.hari.ytlearn.dto.playlistdetails.PlaylistDetailsResponse; // For playlist details response
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(
        YouTubeService.class
    );
    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    // Regex to extract playlist ID from various YouTube URL formats
    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile(
        "[&?]list=([a-zA-Z0-9_-]+)"
    );

    public YouTubeService(
        WebClient.Builder webClientBuilder,
        @Value("${youtube.api.key}") String apiKey,
        @Value("${youtube.api.base-url}") String baseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        log.info("YouTubeService initialized. Base URL: {}", baseUrl);
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_YOUTUBE_API_KEY")) {
            log.warn("YouTube API Key is not configured properly in application.properties!");
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
    public Mono<Playlist> getPlaylistDetails(String playlistUrl) {
        String playlistId = extractPlaylistIdFromUrl(playlistUrl);
        if (playlistId == null || playlistId.isBlank()) {
            log.error("Invalid or missing playlist ID in URL: {}", playlistUrl);
            return Mono.error(
                new IllegalArgumentException(
                    "Invalid or missing playlist ID in URL."
                )
            );
        }

        log.info("Fetching details for playlist ID: {}", playlistId);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromPath("/playlists")
            .queryParam("key", apiKey)
            .queryParam("part", "snippet,contentDetails") // Request snippet and content details
            .queryParam("id", playlistId); // Filter by playlist ID

        URI uri = uriBuilder.build().toUri();
        log.debug("Requesting URI: {}", baseUrl + uri.toString());

        return this.webClient.get()
            .uri(uri)
            .retrieve()
            .onStatus(
                status -> status.isSameCodeAs(HttpStatus.FORBIDDEN),
                clientResponse -> clientResponse
                    .bodyToMono(String.class)
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
            .bodyToMono(PlaylistDetailsResponse.class) // Deserialize into the details response DTO
            .flatMap(response -> {
                // The API returns a list, even when querying by ID.
                // If the list is not empty, return the first item.
                if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
                    Playlist playlist = response.getItems().get(0);
                    log.info(
                        "Successfully fetched details for playlist ID: {} - Title: '{}'",
                        playlistId,
                        playlist.getSnippet() != null ? playlist.getSnippet().getTitle() : "N/A"
                    );
                    return Mono.just(playlist);
                } else {
                    // Playlist ID was valid format, but not found by API
                    log.warn(
                        "Playlist not found for ID: {} (API returned empty list)",
                        playlistId
                    );
                    return Mono.empty(); // Indicate not found
                }
            })
            .doOnError(error ->
                log.error(
                    "Error fetching playlist details for ID: {}",
                    playlistId,
                    error
                )
            );
    }

    // --- Method to get playlist items (from previous example) ---
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
    }
}
