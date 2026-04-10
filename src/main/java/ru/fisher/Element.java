package ru.fisher;

public class Element {
    static final char EMPTY = '0'; // пустой элемент
    char symbol; // хранит один из шести символов A..F
    public Element(char c) {
        this.symbol = c;
    }
}
