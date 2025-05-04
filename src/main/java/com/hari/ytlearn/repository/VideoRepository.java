package com.hari.ytlearn.repository;

import com.hari.ytlearn.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
