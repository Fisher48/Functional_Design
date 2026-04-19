package ru.fisher;

public class Program {

    public static void main(String[] args) {
        BoardState boardState = Game.initializeGame(8);
        boardState = Game.processCascade(boardState);
        boardState.score = 0;
        while (true) {
            Game.draw(boardState);
            boardState = Game.readMove(boardState);
            boardState = Game.processCascade(boardState);
        }
    }
}
