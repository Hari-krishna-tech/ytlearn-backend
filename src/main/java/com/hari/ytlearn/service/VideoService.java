package com.hari.ytlearn.service;

import com.hari.ytlearn.dto.VideoDTO;
import com.hari.ytlearn.model.Note;
import com.hari.ytlearn.model.Video;
import com.hari.ytlearn.repository.NoteRepository;
import com.hari.ytlearn.repository.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private NoteRepository noteRepository;
/*
    private Long id;

    private String youtubePlaylistItemId;

    private String youtubeVideoId;

    private String title;

    private String description;


    private Instant publishedAt;
    // Thumbnail URLs
    private String thumbnailDefaultUrl;
    private String thumbnailMediumUrl;
    private String thumbnailHighUrl;
    private String etag;
    // Information about the channel that uploaded the video
    private String videoOwnerChannelId;
    private String videoOwnerChannelTitle;

    private String position;

    private Status status;
    private Note note;

    private Instant createdAt;
    private Instant updatedAt;
 */

    public ArrayList<VideoDTO> getAllVideosWithPlayListId(Long playListId) {

        ArrayList<Video> videos = videoRepository.findAllByPlayListId(playListId);
        ArrayList<VideoDTO> videoDTOs = new ArrayList<>();
        for (Video video : videos) {
            VideoDTO videoDTO = new VideoDTO();
            videoDTO.setId(video.getId());
            videoDTO.setYoutubePlaylistItemId(video.getYoutubePlaylistItemId());
            videoDTO.setYoutubeVideoId(video.getYoutubeVideoId());
            videoDTO.setTitle(video.getTitle());
            videoDTO.setDescription(video.getDescription());
            videoDTO.setPublishedAt(video.getPublishedAt());
            videoDTO.setThumbnailDefaultUrl(video.getThumbnailDefaultUrl());
            videoDTO.setThumbnailMediumUrl(video.getThumbnailMediumUrl());
            videoDTO.setThumbnailHighUrl(video.getThumbnailHighUrl());
            videoDTO.setEtag(video.getEtag());
            videoDTO.setVideoOwnerChannelId(video.getVideoOwnerChannelId());
            videoDTO.setVideoOwnerChannelTitle(video.getVideoOwnerChannelTitle());
            videoDTO.setPosition(video.getPosition());
            videoDTO.setStatus(video.getStatus());
            videoDTO.setNote(video.getNote());
            videoDTO.setCreatedAt(video.getCreatedAt());
            videoDTO.setUpdatedAt(video.getUpdatedAt());
            videoDTOs.add(videoDTO);

        }

        return videoDTOs;
    }
    /*


      @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", referencedColumnName = "id" )
    private Playlist playlist;

    @Column(nullable = false)
    private String youtubePlaylistItemId;

    @Column(nullable = false)
    private String youtubeVideoId;

    @Column(length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;


    private Instant publishedAt;
    // Thumbnail URLs
    private String thumbnailDefaultUrl;
    private String thumbnailMediumUrl;
    private String thumbnailHighUrl;
    private String etag;
    // Information about the channel that uploaded the video
    private String videoOwnerChannelId;
    private String videoOwnerChannelTitle;

    private String position;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id", referencedColumnName = "id")
    private Note note;


    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

     */
    @Transactional
    public VideoDTO updateVideo(VideoDTO video) {
        Note note = video.getNote();
        Video video1 = videoRepository.findById(video.getId()).get();
        System.out.println(video1);
        if(note!= null) {
            Note savedNote = noteRepository.save(note);
            video.setNote(savedNote);
        }

        if(video.getId() != null) {
            video1.setId(video.getId());
        }
        if(video.getNote() != null) {
            video1.setNote(video.getNote());
        }
        if(video.getStatus() != null) {

            video1.setStatus(video.getStatus());
        }
        System.out.println(video1);
        videoRepository.save(video1);
        return video;
    }
}
