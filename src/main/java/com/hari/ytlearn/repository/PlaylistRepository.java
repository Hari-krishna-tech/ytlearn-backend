package com.hari.ytlearn.repository;


import com.hari.ytlearn.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query(value = "select * from playlist where user_id=:userId", nativeQuery = true)
    public ArrayList<Playlist> findAll(String userId);
}
