package com.hiramgames.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.component.HiramGamesWebSocket;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hiram 2018年11月14日 22:51
 */
@Controller
@RequestMapping("/games")
public class GamesController {

    @RequestMapping("/")
    public String index() {
        return "games/index";
    }

    @RequestMapping("/gobang")
    public String goBang(Model model) {
//        JSONArray roomsJA = new JSONArray();
//        GamesVueController.getRoomsArr(roomsJA);
//        model.addAttribute("rooms", roomsJA);
        return "games/gobangLobby";
    }

    @RequestMapping("/gobanggame")
    public String goBangRoom(String roomName, String player) {
//        if (HiramGamesWebSocket.rooms.keySet().contains(roomName) && HiramGamesWebSocket.rooms.get(roomName).size()<2) {
//            System.out.println("roomName: " + roomName);
//            System.out.println("size: " + HiramGamesWebSocket.rooms.get(roomName).size());
//            HiramGamesWebSocket.rooms.get(roomName).add(player);
//            System.out.println("size: " + HiramGamesWebSocket.rooms.get(roomName).size());
//        }
        return "games/gobang";
    }

    @RequestMapping("/gobanggamenew")
    @ResponseBody
    public JSONArray newGoBangRoom(String player) {
//        System.out.println("new room");
//        List<String> members = new ArrayList<>();
//        members.add(player);
//        HiramGamesWebSocket.rooms.put(player, members);
//        JSONArray roomsJA = new JSONArray();
//        for (String roomName: HiramGamesWebSocket.rooms.keySet()) {
//            System.out.println(roomName);
//            JSONObject roomInfo = new JSONObject();
//            roomInfo.put("name", roomName);
//            roomInfo.put("members", HiramGamesWebSocket.rooms.get(roomName));
//            roomsJA.add(roomInfo);
//        }
//        return roomsJA;
        return null;
    }
}
