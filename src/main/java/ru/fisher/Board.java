package ru.fisher;

public class Board {
    int size;
    Element[][] cells;

    public Board(int s) {
        this.size = s;
        this.cells = new Element[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                cells[x][y] = new Element(Element.EMPTY);
            }
        }
    }
}
