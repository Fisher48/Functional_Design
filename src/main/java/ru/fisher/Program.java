package ru.fisher;

public class Program {

    public static void main(String[] args) {
        BoardState boardState = Game.InitializeGame();
        while (true) {
            Game.draw(boardState.board);
            boardState = Game.readMove(boardState);
        }
    }
}
