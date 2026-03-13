package com.example.battleship.model;

/**
 * Внутреннее состояние клетки.
 */
public enum CellState {
    EMPTY,
    TARGET,
    MISS,
    HIT,
    NEAR // автоматически отмеченная пустая клетка вокруг уничтоженной цели
}


