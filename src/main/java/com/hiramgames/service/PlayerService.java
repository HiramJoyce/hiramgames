package com.hiramgames.service;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.hiramgames.dao.PlayerDao;
import com.hiramgames.domain.Player;
import com.hiramgames.domain.Result;
import com.hiramgames.domain.enums.ResultEnum;
import com.hiramgames.util.EncryptUtil;
import com.hiramgames.util.ResultUtil;
import com.hiramgames.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.regex.Pattern;

@Service
public class PlayerService {
    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    private final String REGEX_USERNAME = "^[a-zA-Z]\\w{3,20}$";
    private final String REGEX_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
    @Resource
    PlayerDao playerDao;

    @Autowired
    private RedisService redisService;

    public Result<?> signIn(String usernameOrEmail, String password) {
        logger.info("--->>> Player attempt to signin with " + usernameOrEmail + " & " + password);
        Player player;
        if (Pattern.matches(REGEX_EMAIL, usernameOrEmail)) {
            player = getPlayerByEmail(usernameOrEmail);
        } else if (Pattern.matches(REGEX_USERNAME, usernameOrEmail)) {
            player = getPlayerByUsername(usernameOrEmail);
        } else {
            return ResultUtil.error(ResultEnum.ERROR);
        }
        if (player == null) {
            return ResultUtil.error(ResultEnum.NO_DATA);
        }
        if (StringUtils.equals(player.getPassword(), EncryptUtil.SHA512(EncryptUtil.SHA512(password) + player.getUsername()))) {
            String token = TokenUtil.generateToken(player.getUsername());
            redisService.set(token, player.getUsername() + "," + player.getNickname() + "," + player.getId(), 60 * 10L);
            JSONObject result = new JSONObject();
            result.put("username", player.getUsername());
            result.put("nickname", player.getNickname());
            result.put("email", player.getEmail());
            result.put("token", token);
            result.put("id", player.getId());
            return ResultUtil.success(result);
        }
        return ResultUtil.error(ResultEnum.ERROR);
    }

    public Result<?> signUp(String username, String nickname, String email, String password) {
        logger.info("--->>> Player attempt to signUp with " + username + " & " + nickname + " & " + email + " & " + password);
        if (!Pattern.matches(REGEX_EMAIL, email) || !Pattern.matches(REGEX_USERNAME, username)) {
            return ResultUtil.error(ResultEnum.ERROR);
        }
        if (getPlayerByUsername(username) != null || getPlayerByEmail(email) != null) {
            return ResultUtil.error(ResultEnum.ERROR);
        }
        Player player = new Player();
        player.setUsername(username);
        player.setNickname(nickname);
        player.setEmail(email);
        player.setPassword(EncryptUtil.SHA512(EncryptUtil.SHA512(password) + username));
        player.setSignUpTime(new Date());
        if (playerDao.insertPlayer(player) > 0) {
            return ResultUtil.success();
        }
        return ResultUtil.error(ResultEnum.ERROR);
    }

    private Player getPlayerByEmail(String email) {
        return playerDao.selectPlayerByEmail(email);
    }

    private Player getPlayerByUsername(String username) {
        return playerDao.selectPlayerByUsername(username);
    }
}
