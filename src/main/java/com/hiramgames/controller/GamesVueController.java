package com.hiramgames.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.component.HiramGamesWebSocket;
import com.hiramgames.domain.Result;
import com.hiramgames.domain.enums.ResultEnum;
import com.hiramgames.service.PlayerService;
import com.hiramgames.service.RedisService;
import com.hiramgames.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GamesVueController {

    private final PlayerService playerService;
    private final RedisService redisService;
    @Autowired
    public GamesVueController(PlayerService playerService, RedisService redisService) {
        this.playerService = playerService;
        this.redisService = redisService;
    }

    @GetMapping("/gobang/rooms")
    public Result<?> getGoBangRooms() {
        JSONArray roomsJA = new JSONArray();
        for (String roomId : HiramGamesWebSocket.rooms.keySet()) {
            roomsJA.add(HiramGamesWebSocket.rooms.get(roomId));
        }
        return ResultUtil.success(roomsJA);
    }

    @PostMapping("/gobang/createRoom")
    public Result<?> createRoom(String roomname, String token) {
        if (StringUtils.isEmpty(roomname) || StringUtils.isEmpty(token)) {
            return ResultUtil.error(ResultEnum.ERROR);
        }
        String redisValue = redisService.get(token);
        if (StringUtils.isEmpty(redisValue)) {
            return ResultUtil.error(ResultEnum.ERROR);
        }
        String username = redisValue.split(",")[0];
        String nickname = redisValue.split(",")[1];
        String newRoomId = username + token.substring(0, 5);
        JSONArray members = new JSONArray();
        JSONObject member = new JSONObject();
        member.put("nickname", nickname);
        member.put("username", username);
        member.put("color", 0);
        members.add(member);
        JSONObject roomInfo = new JSONObject();
        roomInfo.put("id", newRoomId);
        roomInfo.put("members", members);
        roomInfo.put("name", roomname);
        HiramGamesWebSocket.rooms.put(newRoomId, roomInfo);
        return ResultUtil.success(newRoomId);
    }

    @PostMapping("/player/signin")
    public Result<?> signIn(String usernameOrEmail, String password) {
        return playerService.signIn(usernameOrEmail, password);
    }

    @PostMapping("/player/signup")
    public Result<?> signUp(String username, String nickname, String email, String password) {
        return playerService.signUp(username, nickname, email, password);
    }
}
