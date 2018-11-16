package com.hiramgames.component;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Date;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/hiramgames")
@Component
public class HiramGamesWebSocket {

    @Resource
    private RedisService redisService;

    private final Logger logger = LoggerFactory.getLogger(HiramGamesWebSocket.class);
    private static CopyOnWriteArraySet<HiramGamesWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    private Session session;
    private Map<String, String> sessionIdAndUsername = new LinkedHashMap<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static Map<String, JSONObject> rooms = new LinkedHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        logger.info("---> [" + this.session.getId() + "] connected.");
        try {
            JSONObject msg = new JSONObject();
            msg.put("msg", "Welcome");
            msg.put("type", "system");
            msg.put("time", sdf.format(new Date()));
            sendMessage(msg);
        } catch (IOException IOErr) {
            IOErr.printStackTrace();
        }
    }

    private void getRoomsJSONArray(JSONArray roomsJA) {
        for (String roomName: rooms.keySet()) {
            JSONObject roomInfo = new JSONObject();
            roomInfo.put("name", roomName);
            roomInfo.put("members", rooms.get(roomName));
            roomsJA.add(roomInfo);
        }
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        if (rooms.keySet().contains(this.session.getId())) {
            logger.info("此人创建过房间，将删除此房间。");
            rooms.remove(this.session.getId());
            JSONObject msg = new JSONObject();
            msg.put("type", "system");
            msg.put("requireType", "getRooms");
            JSONArray roomsJA = new JSONArray();
            getRoomsJSONArray(roomsJA);
            msg.put("rooms", roomsJA);
            for (HiramGamesWebSocket item : webSocketSet) {
                try {
                    item.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // TODO 判断用户状态，如果在游戏中失去连接判定为逃跑
        logger.info("---> [" + this.session.getId() + "] close the connection.");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject msg = new JSONObject();
        try {
            JSONObject messageObj = JSONObject.parseObject(message);
            logger.info(message);
            switch (messageObj.getString("requireType")) {
                case "joinGame":
                    logger.info("--->>> join game");
                    msg.put("requireType", messageObj.getString("requireType"));
                    msg.put("type", "system");
                    msg.put("time", sdf.format(new Date()));
                    String username = messageObj.getString("username");
                    sessionIdAndUsername.put(username, session.getId());
                    sessionIdAndUsername.put(session.getId(), username);
                    for (String name : sessionIdAndUsername.keySet()) {
                        logger.info(" --- [ " + name + " : " + sessionIdAndUsername.get(name) + " ]");
                    }
                    if (StringUtils.isEmpty(messageObj.getString("roomId")) || rooms.get(messageObj.getString("roomId")) == null) {
                        msg.put("msg", "房间不存在");
                        sendMessage(msg);
                        return;
                    }
                    JSONArray members = rooms.get(messageObj.getString("roomId")).getJSONArray("members");
                    for (Object member1 : members) {
                        JSONObject member = (JSONObject) member1;
                        if (StringUtils.equals(member.getString("username"), username)) {
                            msg.put("msg", "你已在房间中");
                            sendMessage(msg);
                            return;
                        }
                    }
                    String nickname = messageObj.getString("nickname");
                    JSONObject newMember = new JSONObject();
                    newMember.put("username", username);
                    newMember.put("nickname", nickname);
                    rooms.get(messageObj.getString("roomId")).getJSONArray("members").add(newMember);
                    msg.put("msg", rooms.get(messageObj.getString("roomId")));
                    sendMessage(msg);
                    break;
                default:
                    logger.info("default");
                    break;
            }
        } catch (JSONException parseErr) {
            // TODO 数据格式不正确Json化失败
            try {
                msg.put("msg", parseErr.getMessage());
                msg.put("type", "system");
                msg.put("time", sdf.format(new Date()));
                sendMessage(msg);
            } catch (IOException IOErr) {
                IOErr.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("--->>> [" + session.getId() + "] may has something err.");
        error.printStackTrace();
    }

    private void sendMessage(JSONObject message) throws IOException {
        this.session.getBasicRemote().sendText(message.toJSONString());
    }
}
