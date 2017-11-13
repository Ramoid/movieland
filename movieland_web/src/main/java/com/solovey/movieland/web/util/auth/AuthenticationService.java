package com.solovey.movieland.web.util.auth;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solovey.movieland.entity.User;
import com.solovey.movieland.service.UserService;
import com.solovey.movieland.web.util.auth.cache.UserTokenCache;
import com.solovey.movieland.web.util.auth.entity.LoginRequest;
import com.solovey.movieland.web.util.auth.entity.UserToken;
import com.solovey.movieland.web.util.auth.exceptions.BadLoginRequestException;
import com.solovey.movieland.web.util.auth.exceptions.UserNotFoundException;
import com.solovey.movieland.web.util.json.JsonJacksonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuthenticationService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserTokenCache userTokenCache;

    private final UserService userService;

    private final JsonJacksonConverter jsonJacksonConverter;

    @Autowired
    public AuthenticationService(UserTokenCache userTokenCache, UserService userService, JsonJacksonConverter jsonJacksonConverter) {
        this.userTokenCache = userTokenCache;
        this.userService = userService;
        this.jsonJacksonConverter = jsonJacksonConverter;
    }

    public UserToken performLogin(String loginJson) throws UserNotFoundException {
        log.info("Starting login user {}", loginJson);
        long startTime = System.currentTimeMillis();

        LoginRequest loginRequest = jsonJacksonConverter.parseLoginJson(loginJson);
        User user = userService.extractUser(loginRequest.getPassword(), loginRequest.getEmail());
        if (user == null) {
            throw new UserNotFoundException();
        }
        String token = userTokenCache.getUserToken(user);
        UserToken userToken = new UserToken(token, user.getNickname());
        log.info("Successful signing up for user {}. It took {} ms", user.getEmail(), System.currentTimeMillis() - startTime);
        return userToken;


    }

    public void performLogout(String token) {
        log.info("Starting logout token {}", token);
        long startTime = System.currentTimeMillis();
        userTokenCache.removeTokenFromCache(token);
        log.info("User logout done. It took {} ms", System.currentTimeMillis() - startTime);
    }

    public User recognizeUser(String token) {
        log.info("Starting recognize user by token {}", token);
        long startTime = System.currentTimeMillis();
        User user = userTokenCache.findUserByToken(token);
        log.info("User {} is recognized . It took {} ms", user, System.currentTimeMillis() - startTime);
        return user;

    }
}
