package ru.fisher;

import java.util.function.Function;

public class BoardState {
    public final Board board;
    public int score;

    public BoardState(Board board, int score) {
        this.board = board;
        this.score = score;
    }

    public BoardState pipe(Function<BoardState, BoardState> func) {
        return func.apply(this);
    }
}
