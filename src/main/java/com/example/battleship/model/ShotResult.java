package com.example.battleship.model;

import java.io.Serializable;

/**
 * Результат одного выстрела для передачи на слой представления.
 */
public class ShotResult implements Serializable {
    private final boolean valid;
    private final String message;
    private final Boolean hit; // null, если выстрел не выполнен (ошибка/конец игры)
    private final GameStatus status;
    private final GameSession sessionSnapshot;

    private ShotResult(boolean valid, String message, Boolean hit, GameStatus status, GameSession sessionSnapshot) {
        this.valid = valid;
        this.message = message;
        this.hit = hit;
        this.status = status;
        this.sessionSnapshot = sessionSnapshot;
    }

    public static ShotResult invalid(String message) {
        return new ShotResult(false, message, null, null, null);
    }

    public static ShotResult gameFinished(GameStatus status) {
        return new ShotResult(false, "Игра уже завершена", null, status, null);
    }

    public static ShotResult success(boolean hit, GameSession session) {
        return new ShotResult(true, hit ? "Попадание!" : "Промах", hit, session.getStatus(), session);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getHit() {
        return hit;
    }

    public GameStatus getStatus() {
        return status;
    }

    public GameSession getSessionSnapshot() {
        return sessionSnapshot;
    }
}


