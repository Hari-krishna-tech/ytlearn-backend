package com.hari.ytlearn.service;

import com.hari.ytlearn.model.Playlist;
import com.hari.ytlearn.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;


    public Playlist save(String url) {
       // todo fetch playlist data and save
        return null;
    }
    public ArrayList<Playlist> findAll() {
        return (ArrayList<Playlist>) playlistRepository.findAll();
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
