package ru.fisher;

public class Match {
    MatchDirection direction;
    int row;
    int col;
    int length;

    public Match(MatchDirection direction, int row, int col, int length) {
        this.direction = direction;
        this.row = row;
        this.col = col;
        this.length = length;
    }

}
