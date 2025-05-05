package com.hari.ytlearn.service;

import com.hari.ytlearn.model.User;
import com.hari.ytlearn.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional() // Good practice for read operations
    public UserDetails loadUserByUsername(String userIdString)
            throws UsernameNotFoundException {
        try {
            Long userId = Long.parseLong(userIdString); // Convert ID back to Long
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "User not found with ID: " + userIdString
                            )
                    );

            // IMPORTANT: Your User class MUST implement UserDetails
            // Add methods like getAuthorities(), getPassword(), getUsername(),
            // isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired(), isEnabled()
            // For JWT, many can return true, getPassword can return null, getUsername should return the ID.
            // getAuthorities might return an empty list or roles if you have them.
            return user;
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException(
                    "Invalid user ID format: " + userIdString
            );
        }
    }


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
