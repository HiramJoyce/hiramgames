package com.hiramgames.component;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/hiramgames")
@Component
public class HiramGamesWebSocket {

    private final Logger logger = LoggerFactory.getLogger(HiramGamesWebSocket.class);
    private static CopyOnWriteArraySet<HiramGamesWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    private Session session;
    private static Map<String, Session> usernameToSession = new LinkedHashMap<>();
    private static Map<Session, String> sessionToUsername = new LinkedHashMap<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static Map<String, JSONObject> rooms = new LinkedHashMap<>();
    private static Map<String, JSONArray> roomHistory = new LinkedHashMap<>();

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
        String username = sessionToUsername.get(this.session);
        sessionToUsername.remove(this.session);
        usernameToSession.remove(username);
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
                    usernameToSession.put(username, session);
                    sessionToUsername.put(session, username);
                    for (String name : usernameToSession.keySet()) {
                        logger.info(" --- [ " + name + " : " + usernameToSession.get(name).getId() + " ]");
                    }
                    String roomId = messageObj.getString("roomId");
                    if (StringUtils.isEmpty(roomId) || rooms.get(roomId) == null) {
                        return;
                    }
                    JSONArray members = rooms.get(messageObj.getString("roomId")).getJSONArray("members");
                    for (Object member1 : members) {
                        JSONObject member = (JSONObject) member1;
                        if (StringUtils.equals(member.getString("username"), username)) {
                            msg.put("msg", rooms.get(messageObj.getString("roomId")));
                            msg.put("roomHistory", roomHistory.get(roomId));
                            sendMessage(msg);
                            return;
                        }
                    }
                    String nickname = messageObj.getString("nickname");
                    JSONObject newMember = new JSONObject();
                    newMember.put("username", username);
                    newMember.put("nickname", nickname);
                    newMember.put("color", 1);
                    rooms.get(messageObj.getString("roomId")).getJSONArray("members").add(newMember);
                    msg.put("msg", rooms.get(messageObj.getString("roomId")));
                    msg.put("roomHistory", roomHistory.get(roomId));
                    for (Object memberO :rooms.get(messageObj.getString("roomId")).getJSONArray("members")) {
                        JSONObject memberJ = (JSONObject) memberO;
                        logger.info("--->>> 房间进人：向 " + memberJ.getString("username") + "发送消息！");
                        usernameToSession.get(memberJ.getString("username")).getBasicRemote().sendText(msg.toJSONString());
                    }
                    break;
                case "movePiece":
                    logger.info("movePiece!!!");
                    roomId = messageObj.getString("roomId");
                    msg.put("requireType", messageObj.getString("requireType"));
                    msg.put("type", "system");
                    msg.put("time", sdf.format(new Date()));
                    JSONObject point = JSONObject.parseObject(messageObj.getString("point"));

                    // 是否允许落子判断

                    // 输赢判断

                    // 加入房间落子记录
                    JSONArray nowHistory;
                    if (roomHistory.get(roomId)==null) {
                        nowHistory = new JSONArray();
                        roomHistory.put(roomId, nowHistory);
                    }
                    roomHistory.get(roomId).add(point);

                    JSONObject room = rooms.get(roomId);
                    members = room.getJSONArray("members");
                    for (Object memberO : members) {
                        JSONObject memberJ = (JSONObject) memberO;
                        msg.put("move", point);
                        logger.info("---> will send point to " + memberJ.getString("username"));
                        usernameToSession.get(memberJ.getString("username")).getBasicRemote().sendText(msg.toJSONString());
                    }
                    break;
                case "retractApply":
                    logger.info("--->>> retractApply");
                    msg.put("requireType", messageObj.getString("requireType"));
                    msg.put("type", "system");
                    msg.put("time", sdf.format(new Date()));
                    roomId = messageObj.getString("roomId");
                    if (StringUtils.isEmpty(roomId) || rooms.get(roomId) == null) {
                        return;
                    }

                    // 判断如果棋盘上没有申请者的棋子，不允许悔棋（前端已经验证）

                    msg.put("msg", rooms.get(roomId));
                    sendToOther(roomId, messageObj, msg);
                    break;
                case "retractReply":
                    logger.info("--->>> retractReply");
                    msg.put("requireType", messageObj.getString("requireType"));
                    msg.put("type", "system");
                    msg.put("time", sdf.format(new Date()));
                    roomId = messageObj.getString("roomId");
                    if (StringUtils.isEmpty(roomId) || rooms.get(roomId) == null) {
                        return;
                    }
                    if (messageObj.getBoolean("reply")) {

                        // 判断悔棋步数
                        /*
                         * if 最后的棋子是回复的人的 && 历史棋子的数量 > 1 ：
                         *      删两步
                         * else
                         *      删一步
                         */
                        JSONArray history = roomHistory.get(roomId);

                        for (Object memberO : rooms.get(roomId).getJSONArray("members")) {
                            JSONObject memberJ = (JSONObject) memberO;
                            if (StringUtils.equals(memberJ.getString("username"), messageObj.getString("username"))) {
                                if (((JSONObject)history.get(history.size()-1)).getIntValue("color") == memberJ.getIntValue("color") && history.size() > 1) {
                                    roomHistory.get(roomId).remove(roomHistory.get(roomId).size()-1);
                                }
                            }
                        }

                        roomHistory.get(roomId).remove(roomHistory.get(roomId).size()-1);
                        msg.put("msg", "同意悔棋");
                    } else {
                        msg.put("msg", "拒绝悔棋");
                    }
                    msg.put("roomHistory", roomHistory.get(roomId));
                    for(Object memberO : rooms.get(roomId).getJSONArray("members")) {
                        JSONObject memberJ = (JSONObject) memberO;
                        usernameToSession.get(memberJ.getString("username")).getBasicRemote().sendText(msg.toJSONString());
                    }
                    break;
                case "giveUp":
                    logger.info("--->>> giveUp");
                    msg.put("requireType", messageObj.getString("requireType"));
                    msg.put("type", "system");
                    msg.put("time", sdf.format(new Date()));
                    roomId = messageObj.getString("roomId");
                    if (StringUtils.isEmpty(roomId) || rooms.get(roomId) == null) {
                        return;
                    }
                    roomHistory.remove(roomId);
                    sendToOther(roomId, messageObj, msg);
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

    private void sendToOther(String roomId, JSONObject messageObj, JSONObject msg) {
        for(Object memberO : rooms.get(roomId).getJSONArray("members")) {
            JSONObject memberJ = (JSONObject) memberO;
            logger.info(memberJ.getString("username") + "," + messageObj.getString("username"));
            if (!StringUtils.equals(memberJ.getString("username"), messageObj.getString("username"))) {
                try {
                    usernameToSession.get(memberJ.getString("username")).getBasicRemote().sendText(msg.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
