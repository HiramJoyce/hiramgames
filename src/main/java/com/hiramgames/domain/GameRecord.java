package com.hiramgames.domain;

import java.util.Date;

public class GameRecord {
    private int id;
    private int gameId;
    private String playersId;
    private String winnersId;
    private Date startTime;
    private Date endTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getPlayersId() {
        return playersId;
    }

    public void setPlayersId(String playersId) {
        this.playersId = playersId;
    }

    public String getWinnersId() {
        return winnersId;
    }

    public void setWinnersId(String winnersId) {
        this.winnersId = winnersId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "GameRecord{" +
                "id=" + id +
                ", gameId=" + gameId +
                ", playersId='" + playersId + '\'' +
                ", winnersId='" + winnersId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
