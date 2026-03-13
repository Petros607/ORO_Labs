package com.example.battleship.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Состояние одной игровой партии.
 * Хранится целиком в HTTP-сессии.
 */
public class GameSession implements Serializable {

    private final int rows;
    private final int cols;
    private final int totalTargets;
    private int remainingShots;
    private int destroyedTargets;

    private final CellState[][] field;
    private final Set<Coordinate> shots = new HashSet<>();

    private GameStatus status = GameStatus.IN_PROGRESS;

    public GameSession(int rows, int cols, int totalTargets, int shots) {
        this.rows = rows;
        this.cols = cols;
        this.totalTargets = totalTargets;
        this.remainingShots = shots;
        this.field = new CellState[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                field[r][c] = CellState.EMPTY;
            }
        }
        placeTargetsRandomly();
    }

    private void placeTargetsRandomly() {
        Random random = new Random();
        int placed = 0;
        while (placed < totalTargets) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (canPlaceTarget(r, c)) {
                field[r][c] = CellState.TARGET;
                placed++;
            }
        }
    }

    /**
     * Проверяет, можно ли поставить цель в клетку (r,c) с учётом правила
     * "цели не касаются ни сторонами, ни углами".
     */
    private boolean canPlaceTarget(int r, int c) {
        if (field[r][c] == CellState.TARGET) {
            return false;
        }
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr;
                int nc = c + dc;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                    continue;
                }
                if (field[nr][nc] == CellState.TARGET) {
                    return false;
                }
            }
        }
        return true;
    }

    public ShotResult shoot(int row, int col) {
        if (status != GameStatus.IN_PROGRESS) {
            return ShotResult.gameFinished(status);
        }
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return ShotResult.invalid("Некорректные координаты");
        }
        Coordinate coord = new Coordinate(row, col);
        if (shots.contains(coord)) {
            return ShotResult.invalid("Повторный выстрел в ту же клетку запрещён");
        }
        if (remainingShots <= 0) {
            status = GameStatus.LOST;
            return ShotResult.gameFinished(status);
        }

        shots.add(coord);
        remainingShots--;

        CellState current = field[row][col];
        boolean hit;
        if (current == CellState.TARGET) {
            field[row][col] = CellState.HIT;
            hit = true;
            destroyedTargets++;
        } else if (current == CellState.EMPTY) {
            field[row][col] = CellState.MISS;
            hit = false;
        } else {
            // Теоретически не должны сюда попасть, но на всякий случай
            hit = current == CellState.HIT;
        }

        // Проверка завершения игры
        if (destroyedTargets == totalTargets) {
            status = GameStatus.WON;
        } else if (remainingShots == 0) {
            status = GameStatus.LOST;
        }

        return ShotResult.success(hit, this);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getRemainingShots() {
        return remainingShots;
    }

    public int getDestroyedTargets() {
        return destroyedTargets;
    }

    public int getTotalTargets() {
        return totalTargets;
    }

    public GameStatus getStatus() {
        return status;
    }

    public CellState[][] getField() {
        return field;
    }

    public boolean isFinished() {
        return status == GameStatus.WON || status == GameStatus.LOST;
    }
}


