package com.hiramgames.service;

import com.hiramgames.domain.GameRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GameRecordService {
    private final Logger logger = LoggerFactory.getLogger(GameRecordService.class);

    public void savaRecord(GameRecord record) {
        logger.info(record.toString());
    }
}
