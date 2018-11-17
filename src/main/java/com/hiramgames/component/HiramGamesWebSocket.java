package com.hiramgames.component;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    // 使用set保存当前到socket的所有连接
    // onOpen和onClose时都需要主动更新该set
    private static CopyOnWriteArraySet<HiramGamesWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    private Session session;
    // 用户发出joinGame类型的请求后将session和该用户名绑定
    // 方便对局中通过用户名拿到该用户的session发送socket消息
    // 出发onClose或者onError时应该及时更新这两个map
    private static Map<String, Session> usernameToSession = new LinkedHashMap<>();
    private static Map<Session, String> sessionToUsername = new LinkedHashMap<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    // 存储当前所有的游戏房间，键为房间id
    // jsonobject存储了roomId，members（JSONArray），房间名字name等
    // 房间不存在后应根据roomId删除rooms中相应的数据
    public static Map<String, JSONObject> rooms = new LinkedHashMap<>();
    // 存储房间的历史棋子，键为房间id
    // jsonarray中存放jsonobject，jsonobject格式为{x:0,y:0,color:0}
    // 收到movePiece、giveUp、retract类型的请求时需要更新这个map和roomBoardmap
    // 房间不存在后应根据roomId删除roomHistory中相应的数据
    private static Map<String, JSONArray> roomHistory = new LinkedHashMap<>();
    // 用来方便判定输赢的二维数组，键为房间id
    // 值为房间的棋盘二维数组，存的是颜色的值
    // 只要更新roomHistory都应该更新这个map
    // 房间不存在后应根据roomId删除roomBoard中相应的数据
    private static Map<String, Integer[][]> roomBoard = new LinkedHashMap<>();

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

        // if 断开连接的人是否在某房间中
        //    if 该房间中还有其他人
        //       更新房间信息members并通知另一个人
        //    else
        //       删除房间数据和游戏数据

        boolean inRoom = false;
        for (String roomName : rooms.keySet()) {
            JSONObject room = rooms.get(roomName);
            for (Object memberO : room.getJSONArray("members")) {
                JSONObject memberJ = (JSONObject) memberO;
                if (StringUtils.equals(memberJ.getString("username"), username)) {
                    inRoom = true;
                    break;
                }
            }
            if (inRoom) {   // 在当前遍历到的房间中，判断房间中是否有对手
                if (room.getJSONArray("members").size() > 1) {
                    JSONObject msg = new JSONObject();
                    for (Object memberO : room.getJSONArray("members")) {
                        JSONObject memberJ = (JSONObject) memberO;
                        if (!StringUtils.equals(memberJ.getString("username"), username)) {
                            try {
                                msg.put("requireType", "escape");
                                usernameToSession.get(memberJ.getString("username")).getBasicRemote().sendText(msg.toJSONString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }

        sessionToUsername.remove(this.session);
        usernameToSession.remove(username);

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

                    // 加入房间落子记录
                    JSONArray nowHistory;
                    if (roomHistory.get(roomId)==null) {
                        nowHistory = new JSONArray();
                        roomHistory.put(roomId, nowHistory);
                    }
                    roomHistory.get(roomId).add(point);

                    // 判定输赢
                    logger.info("--->>> 判断输赢");
                    int x = point.getIntValue("x");
                    int y = point.getIntValue("y");
                    roomBoard.computeIfAbsent(roomId, k -> new Integer[16][16]);
                    roomBoard.get(roomId)[x][y] = point.getIntValue("color");
                    if (think(x, y, roomBoard.get(roomId))) {
                        logger.info("--->>> 判断输赢完成 得出结果 " + point.getIntValue("color") + " 赢!");
                        logger.info("--->>> 游戏结束清除历史数据");
                        // 游戏结束删掉对局记录
                        roomHistory.remove(roomId);
                        roomBoard.remove(roomId);
                        msg.put("result", point.getIntValue("color"));
                    }

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

                    // 游戏结束删掉对局记录
                    roomHistory.remove(roomId);
                    roomBoard.remove(roomId);

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

    private boolean think (int xIndex, int yIndex, Integer[][] board) {
        int count = 1;
        for(int x=xIndex-1;x>=0;x--){
            if (isEqual(x, yIndex, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        int BOARD_SIZE = 15;
        for(int x = xIndex+1; x<= BOARD_SIZE; x++){
            if (isEqual(x, yIndex, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        if (count>=5) {
            return true;
        }else{
            count = 1;
        }
        for(int y=yIndex-1;y>=0;y--){
            if (isEqual(xIndex, y, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        for(int y = yIndex+1; y<= BOARD_SIZE; y++){
            if (isEqual(xIndex, y, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        if (count>=5) {
            return true;
        }else{
            count = 1;
        }
        for(int x = xIndex+1, y = yIndex-1; y>=0&&x<= BOARD_SIZE; x++,y--){
            if (isEqual(x, y, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        for(int x = xIndex-1, y = yIndex+1; y<= BOARD_SIZE &&x>=0; x--,y++){
            if (isEqual(x, y, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        if (count>=5) {
            return true;
        }else{
            count = 1;
        }
        for(int x=xIndex-1,y=yIndex-1;y>=0&&x>=0;x--,y--){
            if (isEqual(x, y, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        for(int x = xIndex+1, y = yIndex+1; y<= BOARD_SIZE &&x<= BOARD_SIZE; x++,y++){
            if (isEqual(x, y, xIndex, yIndex, board)) {
                count++;
            } else {
                break;
            }
        }
        if (count>=5) {
            return true;
        }else{
            count = 1;
        }
        return false;
    }
    private boolean isEqual(int x, int y, int xIndex, int yIndex, Integer[][] board) {
        return board[x][y] != null && Objects.equals(board[x][y], board[xIndex][yIndex]);
    }
}
