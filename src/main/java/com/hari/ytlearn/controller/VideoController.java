package com.hari.ytlearn.controller;


import com.hari.ytlearn.dto.VideoDTO;
import com.hari.ytlearn.model.Video;
import com.hari.ytlearn.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    @Autowired
    private VideoService videoService;

    @PostMapping("/")
    public VideoDTO updateVideo(@RequestBody VideoDTO video) {

        System.out.println("control reach here");
        System.out.println(video);
        return videoService.updateVideo(video);
    }
}
