package ru.fisher;

import java.util.Random;
import java.util.Scanner;

public class Game {
    static Random r = new Random();
    static Scanner scanner = new Scanner(System.in);
    static char[] symbols = {'A', 'B', 'C', 'D', 'E', 'F'};

    public static void draw(Board board) {
        System.out.println("  0 1 2 3 4 5 6 7");
        for (int i = 0; i < 8; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 8; j++) {
                System.out.print(board.cells[i][j].Symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static Board cloneBoard(Board board) {
        Board b = new Board(board.size);
        for (int row = 0; row < board.size; row++) {
            for (int col = 0; col < board.size; col++) {
                b.cells[row][col] = board.cells[row][col];
            }
        }
        return b;
    }

    public static BoardState readMove(BoardState bs) {
        System.out.println(">");
        String input = scanner.nextLine();
        if (input.equals("q")) {
            System.exit(0);
        }
        Board board = cloneBoard(bs.board);
        String[] coords = input.split("\\s");
        int x = Integer.parseInt(coords[1]);
        int y = Integer.parseInt(coords[0]);
        int x1 = Integer.parseInt(coords[3]);
        int y1 = Integer.parseInt(coords[2]);
        if (isValidMove(board, coords)) {
            Element e = board.cells[x][y];
            board.cells[x][y] = board.cells[x1][y1];
            board.cells[x1][y1] = e;
        } else {
            readMove(bs);
        }
        return new BoardState(board, bs.score);
    }

    public static BoardState InitializeGame() {
        Board newBoard = new Board(8);
        for (int x = 0; x < newBoard.size; x++) {
            for (int y = 0; y < newBoard.size; y++) {
                newBoard.cells[x][y] = new Element(symbols[r.nextInt(symbols.length)]);
            }
        }
        return new BoardState(newBoard,0);
    }

    public static boolean isValidMove(Board board, String[] coords) {
        int x = Integer.parseInt(coords[1]);
        int y = Integer.parseInt(coords[0]);
        int x1 = Integer.parseInt(coords[3]);
        int y1 = Integer.parseInt(coords[2]);
        if (x < 0 || y < 0 || x1 < 0 || y1 < 0 || x >= board.size || y >= board.size || x1 >= board.size || y1 >= board.size) {
            System.out.println("Вы вышли за границы поля. Попробуйте вести корректные данные");
            return false;
        }

        return true;
    }
}
