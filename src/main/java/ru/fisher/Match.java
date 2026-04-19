package ru.fisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Match {
    public final String name;
    public final Position origin;
    public final int width;
    public final int height;
    public final List<Position> pattern;
    public final char symbol;

    public Match(String name, Position origin, int width, int height, List<Position> pattern, char symbol) {
        this.name = name;
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.pattern = Collections.unmodifiableList(pattern);
        this.symbol = symbol;

        // Проверяем, что все относительные позиции находятся внутри прямоугольника
        for (Position pos : this.pattern) {
            if (pos.row < 0 || pos.row >= height || pos.col < 0 || pos.col >= width) {
                throw new IllegalArgumentException("Relative position outside bounds: " + pos);
            }
        }
    }

    public List<Position> getAbsolutePositions() {
        List<Position> absolute = new ArrayList<>();
        for (Position relPos : pattern) {
            absolute.add(new Position(origin.row + relPos.row, origin.col + relPos.col));
        }
        return absolute;
    }
}
