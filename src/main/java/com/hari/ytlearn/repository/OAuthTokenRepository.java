package com.hari.ytlearn.repository;

import com.hari.ytlearn.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    public OAuthToken findByUserId(Long userId);
}
