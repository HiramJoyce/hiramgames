package com.hiramgames.service;

import com.hiramgames.dao.GameRecordDao;
import com.hiramgames.domain.GameRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GameRecordService {
    private final Logger logger = LoggerFactory.getLogger(GameRecordService.class);

    @Resource
    GameRecordDao gameRecordDao;

    public void savaRecord(GameRecord record) {
        logger.info(record.toString());
        gameRecordDao.insertGameRecord(record);
    }
}
