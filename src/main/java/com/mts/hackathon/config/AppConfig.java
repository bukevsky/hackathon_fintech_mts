package com.mts.hackathon.config;

import com.api.igdb.apicalypse.APICalypse;
import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.TwitchAuthenticator;
import com.api.igdb.utils.TwitchToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${twitch.token}")
    private String secret_key;
    @Value("${twitch.client}")
    private String client_id;
    @Value("${igdb.access_token}")
    private String access_token;

    @Bean
    public TwitchToken twitchToken() {
        TwitchAuthenticator tAuth = TwitchAuthenticator.INSTANCE;
        return tAuth.requestTwitchToken(client_id, secret_key);
    }

    @Bean
    public IGDBWrapper igdbWrapper() {
        IGDBWrapper wrapper = IGDBWrapper.INSTANCE;
        wrapper.setCredentials(client_id, access_token);
        return wrapper;
    }

    @Bean
    public APICalypse apiCalypse(){
        return new APICalypse();
    }
}
