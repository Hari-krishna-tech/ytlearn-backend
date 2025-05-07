package com.hari.ytlearn.service;

import com.hari.ytlearn.dto.PlaylistCreateDTO;
import com.hari.ytlearn.dto.PlaylistResponse;
import com.hari.ytlearn.model.Playlist;
import com.hari.ytlearn.model.Status;
import com.hari.ytlearn.model.User;
import com.hari.ytlearn.model.Video;
import com.hari.ytlearn.repository.PlaylistRepository;
import com.hari.ytlearn.repository.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private YouTubeService youTubeService;


    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoService videoService;


    @Transactional // Ensures atomicity: either all save or none
    public Playlist save(String url, User user) {
        // Fetch playlist details from YouTube API
        // Assuming your DTO is com.hari.ytlearn.dto.playlistdetails.Playlist
        com.hari.ytlearn.dto.playlistdetails.Playlist playlistDetailsDto =
                youTubeService.getPlaylistDetails(url);

        // Fetch playlist items (videos) from YouTube API
        // Assuming your DTO is com.hari.ytlearn.dto.playlistitems.PlaylistResponse
        com.hari.ytlearn.dto.PlaylistResponse playlistItemsDto =
                youTubeService.getPlaylistItems(url, 100, null); // Fetches up to 100 items

        if (playlistDetailsDto == null || playlistDetailsDto.getSnippet() == null ||
                playlistDetailsDto.getContentDetails() == null) {
            // Handle error: essential playlist details are missing
            // You might throw an exception or log an error
            System.err.println("Failed to fetch complete playlist details for URL: " + url);
            return null;
        }

        // 1. Create and Populate Playlist Entity
        Playlist playlist = new Playlist();
        playlist.setUser(user); // Associate with the user
        playlist.setYoutubePlaylistId(playlistDetailsDto.getId());
        playlist.setEtag(playlistDetailsDto.getEtag());

        com.hari.ytlearn.dto.playlistdetails.Snippet snippetDto =
                playlistDetailsDto.getSnippet();
        playlist.setTitle(snippetDto.getTitle());
        playlist.setDescription(snippetDto.getDescription());
        if (snippetDto.getPublishedAt() != null) {
            try {
                playlist.setPublishedAt(Instant.parse(snippetDto.getPublishedAt()));
            } catch (Exception e) {
                System.err.println("Error parsing playlist publishedAt: " + snippetDto.getPublishedAt());
            }
        }
        playlist.setChannelId(snippetDto.getChannelId());
        playlist.setChannelTitle(snippetDto.getChannelTitle());

        com.hari.ytlearn.dto.playlistdetails.ContentDetails contentDetailsDto =
                playlistDetailsDto.getContentDetails();
        playlist.setItemCount(contentDetailsDto.getItemCount());

        // Playlist Thumbnails
        if (snippetDto.getThumbnails() != null) {
            com.hari.ytlearn.dto.playlistdetails.Thumbnails thumbnailsDto =
                    snippetDto.getThumbnails();
            if (thumbnailsDto.getDefaultThumbnail() != null) {
                playlist.setThumbnailDefaultUrl(thumbnailsDto.getDefaultThumbnail().getUrl());
            }
            if (thumbnailsDto.getMedium() != null) {
                playlist.setThumbnailMediumUrl(thumbnailsDto.getMedium().getUrl());
            }
            if (thumbnailsDto.getHigh() != null) {
                playlist.setThumbnailHighUrl(thumbnailsDto.getHigh().getUrl());
            }
            if (thumbnailsDto.getStandard() != null) {
                playlist.setThumbnailStandardUrl(thumbnailsDto.getStandard().getUrl());
            }
            if (thumbnailsDto.getMaxres() != null) {
                playlist.setThumbnailMaxresUrl(thumbnailsDto.getMaxres().getUrl());
            }
        }

        playlist.setLastSyncedAt(Instant.now());
        // 'createdAt' and 'updatedAt' are handled by @PrePersist/@PreUpdate

        // Save the Playlist entity
        Playlist savedPlaylist = playlistRepository.save(playlist);

        // 2. Create and Populate Video Entities
        if (playlistItemsDto != null && playlistItemsDto.getItems() != null) {
            for (com.hari.ytlearn.dto.PlaylistItem item :
                    playlistItemsDto.getItems()) {
                if (item.getSnippet() == null || item.getSnippet().getResourceId() == null) {
                    System.err.println("Skipping playlist item due to missing snippet or resourceId: " + item.getId());
                    continue;
                }

                Video video = new Video();
                video.setPlaylist(savedPlaylist); // Set the foreign key relationship

                video.setYoutubePlaylistItemId(item.getId()); // ID of the item in the playlist
                video.setEtag(item.getEtag());

                com.hari.ytlearn.dto.Snippet itemSnippet =
                        item.getSnippet();
                video.setYoutubeVideoId(itemSnippet.getResourceId().getVideoId());
                video.setTitle(itemSnippet.getTitle());
                video.setDescription(itemSnippet.getDescription());
                if (itemSnippet.getPublishedAt() != null) {
                    try {
                        video.setPublishedAt(Instant.parse(itemSnippet.getPublishedAt()));
                    } catch (Exception e) {
                        System.err.println("Error parsing video publishedAt: " + itemSnippet.getPublishedAt());
                    }
                }
                video.setPosition(String.valueOf(itemSnippet.getPosition()));

                // Video Thumbnails
                if (itemSnippet.getThumbnails() != null) {
                    com.hari.ytlearn.dto.Thumbnails videoThumbs =
                            itemSnippet.getThumbnails();
                    // Assuming PlaylistItemThumbnails has methods returning Thumbnail DTOs
                    if (videoThumbs.getDefaultThumbnail() != null) {
                        video.setThumbnailDefaultUrl(videoThumbs.getDefaultThumbnail().getUrl());
                    }
                    if (videoThumbs.getMedium() != null) {
                        video.setThumbnailMediumUrl(videoThumbs.getMedium().getUrl());
                    }
                    if (videoThumbs.getHigh() != null) {
                        video.setThumbnailHighUrl(videoThumbs.getHigh().getUrl());
                    }
                }

                video.setVideoOwnerChannelId(itemSnippet.getVideoOwnerChannelId());
                video.setVideoOwnerChannelTitle(itemSnippet.getVideoOwnerChannelTitle());

                video.setStatus(Status.INCOMPLETE); // Set a default status
                // 'createdAt' and 'updatedAt' are handled by @PrePersist/@PreUpdate

                videoRepository.save(video);
            }
        }
        return savedPlaylist;
    }
    public ArrayList<PlaylistCreateDTO> findAll() {
        ArrayList<Playlist> answer = (ArrayList<Playlist>) playlistRepository.findAll();
        ArrayList<PlaylistCreateDTO> result = new ArrayList<>();
        for (Playlist playlist : answer) {
            PlaylistCreateDTO dto = new PlaylistCreateDTO();
// Map fields from Playlist entity to PlaylistCreateDTO
            dto.setYoutubePlaylistId(playlist.getYoutubePlaylistId());
            dto.setTitle(playlist.getTitle());
            dto.setDescription(playlist.getDescription());
            dto.setPublishedAt(playlist.getPublishedAt());
            dto.setChannelId(playlist.getChannelId());
            dto.setChannelTitle(playlist.getChannelTitle());
            dto.setItemCount(playlist.getItemCount()); // Autoboxing from int to Integer

            // Thumbnail URLs
            dto.setThumbnailDefaultUrl(playlist.getThumbnailDefaultUrl());
            dto.setThumbnailMediumUrl(playlist.getThumbnailMediumUrl());
            dto.setThumbnailHighUrl(playlist.getThumbnailHighUrl());
            dto.setThumbnailStandardUrl(playlist.getThumbnailStandardUrl());
            dto.setThumbnailMaxresUrl(playlist.getThumbnailMaxresUrl());

            // Fields not in PlaylistCreateDTO (like id, user, etag, lastSyncedAt, createdAt, updatedAt)
            // will not be mapped, which is consistent with the DTO's definition.
            ArrayList<Video> videos = (ArrayList<Video>) videoService.getAllVideosWithPlayListId(playlist.getId());
            int completedCount = 0;
            for (Video video : videos) {
                if (video.getStatus() == Status.COMPLETE) {
                    completedCount++;
                }
            }

            dto.setTotalCompletions(completedCount);
            result.add(dto);
        }
        return result;
    }

    public Playlist findById(Long id) {
        return playlistRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        playlistRepository.deleteById(id);
    }
    public void deleteAll() {
        playlistRepository.deleteAll();
    }

}
