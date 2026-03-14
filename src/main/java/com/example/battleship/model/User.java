package com.example.battleship.model;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String nickname;
    private int countOfWins;

    public User() {
    }

    public User(int id, String nickname, int countOfWins) {
        this.id = id;
        this.nickname = nickname;
        this.countOfWins = countOfWins;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getCountOfWins() {
        return countOfWins;
    }

    public void setCountOfWins(int countOfWins) {
        this.countOfWins = countOfWins;
    }
}
