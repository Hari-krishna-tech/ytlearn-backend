package com.hari.ytlearn.service;

import com.hari.ytlearn.model.User;
import com.hari.ytlearn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        User updateUser = new User();

        updateUser.setId(user.getId());
        if(user.getName()!= null) {
            updateUser.setName(user.getName());
        }
        if(user.getEmail()!= null) {
            updateUser.setEmail(user.getEmail());
        }
        if(user.getGoogleId() != null) {
            updateUser.setGoogleId(user.getGoogleId());
        }

        if(user.getProfilePicture() != null) {
            updateUser.setProfilePicture(user.getProfilePicture());
        }
        updateUser.setUpdatedAt(Instant.now());

        return userRepository.save(updateUser);
    }


    public User findByGoogleId(String googleId) {
        User user = userRepository.findByGoogleId(googleId);
        if(user == null) {
            return null;
        }
        return user;
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
