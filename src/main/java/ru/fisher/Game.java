package ru.fisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;

public class Game {
    static Random r = new Random();
    static Scanner scanner = new Scanner(System.in);
    static char[] symbols = {'A', 'B', 'C', 'D', 'E', 'F'};

    public static BoardState draw(BoardState bs) {
        Board board = bs.board;
        System.out.println("  0 1 2 3 4 5 6 7");
        for (int i = 0; i < 8; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 8; j++) {
                System.out.print(board.cells[i][j].symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
        return bs;
    }

    public static Board cloneBoard(Board board) {
        Board b = new Board(board.size);
        for (int row = 0; row < board.size; row++) {
            for (int col = 0; col < board.size; col++) {
                b.cells[row][col] = new Element(board.cells[row][col].symbol);
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

    public static BoardState initializeGame(int boardSize) {
        return runPipeline(
                new BoardState(new Board(boardSize), 0),
                Game::fillEmptySpace,
                Game::processCascade
        );
    }

    public static boolean isValidMove(Board board, String[] coords) {
        if (coords.length != 4) {
            System.out.println("Введите 4 числа: x y x1 y1");
            return false;
        }


        int x = Integer.parseInt(coords[1]);
        int y = Integer.parseInt(coords[0]);
        int x1 = Integer.parseInt(coords[3]);
        int y1 = Integer.parseInt(coords[2]);
        if (x < 0 || y < 0 || x1 < 0 || y1 < 0 || x >= board.size || y >= board.size || x1 >= board.size || y1 >= board.size) {
            System.out.println("Вы вышли за границы поля. Попробуйте вести корректные данные");
            return false;
        }

        int dx = Math.abs(x - x1);
        int dy = Math.abs(y - y1);
        if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) {
            System.out.println("Можно менять только соседние элементы. \n" +
                    "Например x:1 y:1 x1:0 y1:1");
            return false;
        }

        return true;
    }

    private static void addMatchIfValid(List<Match> matches, int row, int col,
                                        int length, MatchDirection direction) {
        // Учитываем только комбинации из 3 и более
        if (length >= 3) {
            matches.add(new Match(direction, row, col, length));
        }
    }

    public static List<Match> findMatches(Board board) {
        List<Match> matches = new ArrayList<>();

        // Горизонтальные комбинации
        for (int row = 0; row < board.size; row++) {
            int startCol = 0;

            for (int col = 1; col < board.size; col++) {
                // Пропускаем пустые ячейки в начале строки
                if (board.cells[row][startCol].symbol == Element.EMPTY) {
                    startCol = col;
                    continue;
                }
                // Если текущая ячейка пустая, обрываем текущую последовательность
                if (board.cells[row][col].symbol == Element.EMPTY) {
                    addMatchIfValid(matches, row, startCol, col - startCol, MatchDirection.HORIZONTAL);
                    startCol = col + 1;
                    continue;
                }
                // Проверяем совпадение для непустых ячеек
                if (board.cells[row][col].symbol != board.cells[row][startCol].symbol) {
                    addMatchIfValid(matches, row, startCol, col - startCol, MatchDirection.HORIZONTAL);
                    startCol = col;
                } else if (col == board.size - 1) {
                    addMatchIfValid(matches, row, startCol, col - startCol + 1, MatchDirection.HORIZONTAL);
                }
            }
        }

        // Вертикальные комбинации
        for (int col = 0; col < board.size; col++) {
            int startRow = 0;

            for (int row = 1; row < board.size; row++) {
                // Пропускаем пустые ячейки в начале строки
                if (board.cells[startRow][col].symbol == Element.EMPTY) {
                    startRow = row;
                    continue;
                }

                // Если текущая ячейка пустая, обрываем текущую последовательность
                if (board.cells[row][col].symbol == Element.EMPTY) {
                    addMatchIfValid(matches, startRow, col, row - startRow, MatchDirection.VERTICAL);
                    startRow = row + 1;
                    continue;
                }

                // Проверяем совпадение для непустых ячеек
                if (board.cells[row][col].symbol != board.cells[startRow][col].symbol) {
                    addMatchIfValid(matches, startRow, col, row - startRow, MatchDirection.VERTICAL);
                    startRow = row;
                } else if (row == board.size - 1) {
                    addMatchIfValid(matches, startRow, col, row - startRow + 1, MatchDirection.VERTICAL);
                }
            }
        }

        return matches;
    }

    public static BoardState removeMatches(BoardState currentState, List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return currentState;
        }

        // Шаг 1. Помечаем ячейки для удаления
        Element[][] markedCells = markCellsForRemoval(currentState.board, matches);

        // Шаг 2. Применяем гравитацию
        Element[][] gravityAppliedCells = applyGravity(markedCells, currentState.board.size);

        // Шаг 3. Подсчитываем очки
        int removedCount = matches.stream()
                .mapToInt(m -> m.length)
                .sum();

        int newScore = currentState.score + calculateScore(removedCount);

        BoardState newBoardState = new BoardState(new Board(currentState.board.size), newScore);
        newBoardState.board.cells = gravityAppliedCells;

        return newBoardState;
    }



    private static Element[][] applyGravity(Element[][] cells, int size) {
        Element[][] newCells = new Element[size][size];

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                newCells[row][col] = new Element(Element.EMPTY);
            }
        }

        for (int col = 0; col < size; col++) {
            int newRow = size - 1;
            for (int row = size - 1; row >= 0; row--) {
                if (cells[row][col].symbol != Element.EMPTY) {
                    newCells[newRow][col] = cells[row][col];
                    newRow--;
                }
            }
        }

        return newCells;
    }

    private static Element[][] markCellsForRemoval(Board board, List<Match> matches) {
        Element[][] newCells = deepCopy(board.cells, board.size);
        for (Match m : matches) {
            for (int i = 0; i < m.length; i++) {
                int row = m.direction == MatchDirection.HORIZONTAL ? m.row : m.row + i;
                int col = m.direction == MatchDirection.HORIZONTAL ? m.col + i : m.col;

                newCells[row][col] = new Element(Element.EMPTY);
            }
        }
        return newCells;
    }

    private static int calculateScore(int removedCount) {
        // Базовая система подсчета очков: 10 за каждый элемент
        return removedCount * 10;
    }

    public static BoardState fillEmptySpace(BoardState currentState) {
        if (currentState.board.cells == null) {
            return currentState;
        }

        Element[][] newCells = deepCopy(currentState.board.cells, currentState.board.size);

        for (int row = 0; row < currentState.board.size; row++) {
            for (int col = 0; col < currentState.board.size; col++) {
                if (newCells[row][col].symbol == Element.EMPTY) {
                    newCells[row][col] = new Element(symbols[r.nextInt(symbols.length)]);
                }
            }
        }

        BoardState newBoardState = new BoardState(new Board(currentState.board.size), currentState.score);
        newBoardState.board.cells = newCells;

        return newBoardState;
    }

    public static Element[][] deepCopy(Element[][] original, int size) {
        Element[][] copy = new Element[size][size];

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                copy[row][col] = new Element(original[row][col].symbol);
            }
        }

        return copy;
    }

    @SafeVarargs
    public static <T> T runPipeline(T input, Function<T, T>... steps) {
        T result = input;
        for (Function<T, T> step : steps) {
            result = step.apply(result);
        }
        return result;
    }


    public static BoardState processCascade(BoardState state) {
        return findMatches(state.board).isEmpty()
                ? state
                : runPipeline(
                        state,
                bs -> removeMatches(bs, findMatches(bs.board)),
                Game::fillEmptySpace,
                Game::processCascade
        );
    }
}
