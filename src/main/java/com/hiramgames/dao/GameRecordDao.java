package com.hiramgames.dao;

import com.hiramgames.domain.GameRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GameRecordDao {
    int insertGameRecord(GameRecord gameRecord);
}
