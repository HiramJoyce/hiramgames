package com.hiramgames.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.dao.GameDao;
import com.hiramgames.domain.Game;
import com.hiramgames.domain.Result;
import com.hiramgames.util.ResultUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class GameService {
    @Resource
    GameDao gameDao;

    public Result<?> getGames() {
        JSONArray gameArr = new JSONArray();
        List<Game> games = gameDao.findGames();
        JSONObject game;
        for (Game gameEach : games) {
            game = new JSONObject();
            game.put("id", gameEach.getId());
            game.put("cnname", gameEach.getCnName());
            game.put("enname", gameEach.getEnName());
            game.put("uptime", gameEach.getUpTime());
            gameArr.add(game);
        }
        return ResultUtil.success(gameArr);
    }
}
