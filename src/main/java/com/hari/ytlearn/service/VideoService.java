package com.hari.ytlearn.service;

import com.hari.ytlearn.model.Video;
import com.hari.ytlearn.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;


    public ArrayList<Video> getAllVideosWithPlayListId(Long playListId) {
        return videoRepository.findAllByPlayListId(playListId);
    }
}
