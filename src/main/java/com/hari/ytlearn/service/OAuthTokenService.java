package com.hari.ytlearn.service;

import com.hari.ytlearn.model.OAuthToken;
import com.hari.ytlearn.repository.OAuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OAuthTokenService {
    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;

    public OAuthToken save(OAuthToken oAuthToken) {


        return oAuthTokenRepository.save(oAuthToken);

    }

    public OAuthToken update(OAuthToken oAuthToken) {
        OAuthToken updatedOAuthToken = new OAuthToken();

        updatedOAuthToken.setId(oAuthToken.getId());
        updatedOAuthToken.setAccessToken(oAuthToken.getAccessToken());
        updatedOAuthToken.setRefreshToken(oAuthToken.getRefreshToken());

        return oAuthTokenRepository.save(updatedOAuthToken);
    }

    public OAuthToken getById(Long id) {
        return oAuthTokenRepository.findById(id).orElse(null);

    }

    public OAuthToken getTokenByUserId(Long userId) {
        return oAuthTokenRepository.findByUserId(userId);
    }


    public String getAccessToken(Long id) {
        OAuthToken oAuthToken = getById(id);

        if(oAuthToken!= null) {
            return oAuthToken.getAccessToken();
        }

        return null;
    }

    public String getRefreshToken(Long id) {
        OAuthToken oAuthToken = getById(id);
        if(oAuthToken!= null) {
            return oAuthToken.getRefreshToken();

        }
        return null;
    }

    public void delete(Long id) {
        oAuthTokenRepository.deleteById(id);
    }

}
