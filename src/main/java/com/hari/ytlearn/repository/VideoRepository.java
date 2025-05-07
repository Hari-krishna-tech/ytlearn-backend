package com.hari.ytlearn.repository;

import com.hari.ytlearn.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;

public interface VideoRepository extends JpaRepository<Video, Long> {


    @Query(value = "SELECT * FROM videos WHERE playlist_id =:playListId", nativeQuery = true)
    public ArrayList<Video> findAllByPlayListId(long playListId);
}
