package com.hiramgames.dao;

import com.hiramgames.domain.Game;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GameDao {
    List<Game> findGames();
}
