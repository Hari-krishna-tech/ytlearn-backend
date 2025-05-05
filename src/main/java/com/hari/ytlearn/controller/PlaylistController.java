package com.hari.ytlearn.controller;


import com.hari.ytlearn.model.Playlist;
import com.hari.ytlearn.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @GetMapping("/")
    public ResponseEntity<?> getAllPlaylist() {
        ArrayList<Playlist> listOfPlaylist = playlistService.findAll();
        if(listOfPlaylist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(playlistService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaylistById(Long id) {
        Playlist playlist = playlistService.findById(id);
        if(playlist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(playlist);
    }

    @PostMapping("/")
    public ResponseEntity<?> createPlaylist(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        if(name == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Playlist playlist = playlistService.save(name);
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(Long id) {
        try {
            playlistService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllPlaylist() {
        try {
            playlistService.deleteAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
