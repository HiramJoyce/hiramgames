package com.hiramgames.dao;

import com.hiramgames.domain.Player;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlayerDao {
    Player selectPlayerByUsername(String userName); // 多个参数时需要用@Param注解
    Player selectPlayerByEmail(String email);
    int insertPlayer(Player player);
}