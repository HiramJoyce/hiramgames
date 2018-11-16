package com.hiramgames.controller;

import com.hiramgames.domain.Result;
import com.hiramgames.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/singin")
    public Result<?> signIn(String usernameOrEmail, String password) {
        return playerService.signIn(usernameOrEmail, password);
    }

    @PostMapping("/singup")
    public Result<?> signUp(String username, String email, String password) {
        return playerService.signUp(username, email, password);
    }
}
