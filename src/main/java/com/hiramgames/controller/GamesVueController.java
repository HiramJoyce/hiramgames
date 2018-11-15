package com.hiramgames.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.component.HiramGamesWebSocket;
import com.hiramgames.domain.Result;
import com.hiramgames.util.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GamesVueController {

    @GetMapping("/gobangRooms")
    public Result<?> getGoBangRooms() {
        JSONArray roomsJA = new JSONArray();
        getRoomsArr(roomsJA);
        return ResultUtil.success(roomsJA);
    }

    static void getRoomsArr(JSONArray roomsJA) {
        for (String roomName: HiramGamesWebSocket.rooms.keySet()) {
            System.out.println(roomName);
            JSONObject roomInfo = new JSONObject();
            roomInfo.put("name", roomName);
            roomInfo.put("members", HiramGamesWebSocket.rooms.get(roomName));
            System.out.println(HiramGamesWebSocket.rooms.get(roomName).size());
            roomsJA.add(roomInfo);
        }
    }
}
