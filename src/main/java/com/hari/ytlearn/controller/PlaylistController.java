package com.hari.ytlearn.controller;


import com.hari.ytlearn.model.Playlist;
import com.hari.ytlearn.model.User;
import com.hari.ytlearn.service.JwtService;
import com.hari.ytlearn.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private JwtService jwtService;


    @Autowired
    private UserDetailsService userDetailsService; // Use UserDetailsService


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
    public ResponseEntity<?> createPlaylist(
            @CookieValue(
                    name = "${jwt.cookie.access-token-name}",
                    required = false
            ) String accessToken,
            @RequestBody Map<String, Object> payload) {


        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token not found in cookie.");
        }

        String userId = null;
            // Validate the refresh token itself (check signature, expiry)
        if (!jwtService.isTokenValid(accessToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired refresh token.");
            }
        userId = jwtService.extractUserId(accessToken);

            // Load user details using the ID from the token
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);



            String url = (String) payload.get("url");
        if(url== null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Playlist playlist = playlistService.save(url, (User) userDetails);
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
