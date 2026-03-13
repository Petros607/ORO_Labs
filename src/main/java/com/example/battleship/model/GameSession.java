package com.example.battleship.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Состояние одной игровой партии.
 * Хранится целиком в HTTP-сессии.
 */
public class GameSession implements Serializable {

    private static final Logger log = Logger.getLogger(GameSession.class.getName());

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
        log.log(Level.FINE, "GameSession created: rows={0}, cols={1}, totalTargets={2}, shots={3}",
                new Object[]{rows, cols, totalTargets, shots});
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
            log.log(Level.FINE, "Shot ignored: game already finished, status={0}", status);
            return ShotResult.gameFinished(status);
        }
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            log.log(Level.FINE, "Shot with out-of-bounds coordinates: row={0}, col={1}",
                    new Object[]{row, col});
            return ShotResult.invalid("Некорректные координаты");
        }
        Coordinate coord = new Coordinate(row, col);
        if (shots.contains(coord)) {
            log.log(Level.FINE, "Repeated shot at the same cell: row={0}, col={1}",
                    new Object[]{row, col});
            return ShotResult.invalid("Повторный выстрел в ту же клетку запрещён");
        }
        if (remainingShots <= 0) {
            status = GameStatus.LOST;
            log.log(Level.FINE, "Shot ignored: no remaining shots, game lost");
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
            markNeighborsAsNear(row, col);
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

        log.log(Level.FINE,
                "Shot applied: row={0}, col={1}, hit={2}, status={3}, remainingShots={4}, destroyedTargets={5}",
                new Object[]{row, col, hit, status, remainingShots, destroyedTargets});

        return ShotResult.success(hit, this);
    }

    /**
     * Отмечает все соседние клетки вокруг уничтоженной цели как гарантированно пустые (NEAR),
     * если по ним ещё не стреляли.
     */
    private void markNeighborsAsNear(int r, int c) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int nr = r + dr;
                int nc = c + dc;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                    continue;
                }
                if (field[nr][nc] == CellState.EMPTY) {
                    field[nr][nc] = CellState.NEAR;
                }
            }
        }
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


