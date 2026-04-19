package ru.fisher;

import java.util.*;
import java.util.function.Function;

public class Game {
    static final Random r = new Random();
    static final Scanner scanner = new Scanner(System.in);
    static final char[] SYMBOLS = {'A', 'B', 'C', 'D', 'E', 'F'};

    // ------------------- Генерация паттернов для уровня -------------------
    public static List<Match> generateLevelMatches() {
        List<Match> patterns = new ArrayList<>();

        // Горизонтальная 3 в ряд (ширина 3, высота 1)
        patterns.add(new Match(
                "Horizontal_3",
                new Position(0, 0),  // origin временный, будет заменён при поиске
                3, 1,
                List.of(new Position(0, 0),
                        new Position(0, 1),
                        new Position(0, 2)),
                '?'  // символ будет определён при поиске
        ));

        // Вертикальная 3 в ряд (ширина 1, высота 3)
        patterns.add(new Match(
                "Vertical_3",
                new Position(0, 0),
                1, 3,
                List.of(new Position(0, 0),
                        new Position(1, 0),
                        new Position(2, 0)),
                '?'
        ));

        // Горизонтальная 4 (дополнительно, для бонуса)
        patterns.add(new Match(
                "Horizontal_4",
                new Position(0, 0),
                4, 1,
                List.of(new Position(0, 0),
                        new Position(0, 1),
                        new Position(0, 2),
                        new Position(0, 3)),
                '?'
        ));

        // Вертикальная 4
        patterns.add(new Match(
                "Vertical_4",
                new Position(0, 0),
                1, 4,
                List.of(new Position(0, 0),
                        new Position(1, 0),
                        new Position(2, 0),
                        new Position(3, 0)),
                '?'
        ));

        // Квадрат 2x2
        patterns.add(new Match(
                "Square_2x2",
                new Position(0, 0),
                2, 2,
                List.of(new Position(0, 0),
                        new Position(0, 1),
                        new Position(1, 0),
                        new Position(1, 1)),
                '?'
        ));

        return patterns;
    }

    // ------------------- Поиск комбинаций -------------------
    public static List<Match> findMatches(Board board, List<Match> patterns) {
        List<Match> found = new ArrayList<>();

        for (Match pattern : patterns) {
            if (pattern.pattern.isEmpty()) continue;

            int maxRow = board.size - pattern.height;
            int maxCol = board.size - pattern.width;
            for (int row = 0; row <= maxRow; row++) {
                for (int col = 0; col <= maxCol; col++) {
                    // Определим символ первой клетки паттерна
                    Position firstRel = pattern.pattern.getFirst();
                    int firstAbsRow = row + firstRel.row;
                    int firstAbsCol = col + firstRel.col;
                    char firstSymbol = board.cells[firstAbsRow][firstAbsCol].symbol;
                    if (firstSymbol == Element.EMPTY) continue;

                    boolean match = true;
                    for (Position rel : pattern.pattern) {
                        int absRow = row + rel.row;
                        int absCol = col + rel.col;
                        char sym = board.cells[absRow][absCol].symbol;
                        if (sym == Element.EMPTY || sym != firstSymbol) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        found.add(new Match(
                                pattern.name,
                                new Position(row, col),
                                pattern.width,
                                pattern.height,
                                pattern.pattern,
                                firstSymbol
                        ));
                    }
                }
            }
        }
        return found;
    }

    // ------------------- Удаление комбинаций -------------------
    public static BoardState removeMatches(BoardState state, List<Match> matches) {
        if (matches.isEmpty()) return state;

        Board newBoard = cloneBoard(state.board);
        for (Match match : matches) {
            for (Position pos : match.getAbsolutePositions()) {
                if (pos.row >= 0 && pos.row < newBoard.size && pos.col >= 0 && pos.col < newBoard.size) {
                    newBoard.cells[pos.row][pos.col] = new Element(Element.EMPTY);
                }
            }
        }

        // Подсчёт очков (можно сделать по-разному, например, за длину + бонус за тип)
        int totalRemoved = matches.stream().mapToInt(m -> m.pattern.size()).sum();
        int scoreGain = calculateScore(totalRemoved);
        int newScore = state.score + scoreGain;

        return new BoardState(newBoard, newScore);
    }

    private static int calculateScore(int removedCount) {
        return removedCount * 10;
    }

    // ------------------- Гравитация -------------------
    private static Element[][] applyGravity(Element[][] cells, int size) {
        Element[][] newCells = new Element[size][size];
        for (int row = 0; row < size; row++)
            for (int col = 0; col < size; col++)
                newCells[row][col] = new Element(Element.EMPTY);

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

    // Применяем гравитацию к BoardState (чистая функция)
    public static BoardState applyGravity(BoardState state) {
        Element[][] newCells = applyGravity(state.board.cells, state.board.size);
        Board newBoard = new Board(state.board.size);
        newBoard.cells = newCells;
        return new BoardState(newBoard, state.score);
    }

    // ------------------- Заполнение пустот -------------------
    public static BoardState fillEmptySpace(BoardState state) {
        Element[][] newCells = deepCopy(state.board.cells, state.board.size);
        for (int row = 0; row < state.board.size; row++) {
            for (int col = 0; col < state.board.size; col++) {
                if (newCells[row][col].symbol == Element.EMPTY) {
                    newCells[row][col] = new Element(SYMBOLS[r.nextInt(SYMBOLS.length)]);
                }
            }
        }
        Board newBoard = new Board(state.board.size);
        newBoard.cells = newCells;
        return new BoardState(newBoard, state.score);
    }

    // ------------------- Вспомогательные -------------------
    public static Board cloneBoard(Board board) {
        Board copy = new Board(board.size);
        for (int i = 0; i < board.size; i++)
            System.arraycopy(board.cells[i], 0, copy.cells[i], 0, board.size);
        return copy;
    }

    public static Element[][] deepCopy(Element[][] original, int size) {
        Element[][] copy = new Element[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                copy[i][j] = new Element(original[i][j].symbol);
        return copy;
    }

    // ------------------- Конвейер -------------------
    @SafeVarargs
    public static <T> T runPipeline(T input, Function<T, T>... steps) {
        T result = input;
        for (Function<T, T> step : steps)
            result = step.apply(result);
        return result;
    }

    // ------------------- Основной игровой процесс -------------------
    public static BoardState processCascade(BoardState state) {
        List<Match> patterns = generateLevelMatches();
        List<Match> matches = findMatches(state.board, patterns);
        if (matches.isEmpty()) return state;

        return runPipeline(
                state,
                s -> removeMatches(s, matches),
                Game::applyGravity,
                Game::fillEmptySpace,
                Game::processCascade
        );
    }

    public static BoardState initializeGame(int boardSize) {
        Board empty = new Board(boardSize);
        BoardState state = new BoardState(empty, 0);
        // Заполняем пустоты случайными фишками и сразу прогоняем каскады
        return runPipeline(state, Game::fillEmptySpace, Game::processCascade);
    }

    // ------------------- Ввод/вывод -------------------
    public static BoardState draw(BoardState bs) {
        Board board = bs.board;
        System.out.println("  0 1 2 3 4 5 6 7");
        for (int i = 0; i < board.size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < board.size; j++) {
                System.out.print(board.cells[i][j].symbol + " ");
            }
            System.out.println();
        }
        System.out.println("Очки: " + bs.score);
        return bs;
    }

    public static BoardState readMove(BoardState state) {
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            if (input.equals("q")) System.exit(0);
            String[] parts = input.split("\\s");
            if (parts.length != 4) {
                System.out.println("Нужно 4 числа: строка столбец строка столбец");
                continue;
            }
            try {
                int row1 = Integer.parseInt(parts[0]);
                int col1 = Integer.parseInt(parts[1]);
                int row2 = Integer.parseInt(parts[2]);
                int col2 = Integer.parseInt(parts[3]);

                if (!isValidMove(state.board, row1, col1, row2, col2)) {
                    System.out.println("Неверный ход (только соседние клетки, в пределах поля)");
                    continue;
                }

                Board newBoard = cloneBoard(state.board);
                Element tmp = newBoard.cells[row1][col1];
                newBoard.cells[row1][col1] = newBoard.cells[row2][col2];
                newBoard.cells[row2][col2] = tmp;

                // Проверяем, создаёт ли ход хотя бы одну комбинацию
                List<Match> patterns = generateLevelMatches();
                if (findMatches(newBoard, patterns).isEmpty()) {
                    System.out.println("Этот ход не создаёт комбинаций, попробуйте другой.");
                    continue;
                }

                return new BoardState(newBoard, state.score);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите числа");
            }
        }
    }

    private static boolean isValidMove(Board board, int row1, int col1, int row2, int col2) {
        if (row1 < 0 || row1 >= board.size || col1 < 0 || col1 >= board.size ||
                row2 < 0 || row2 >= board.size || col2 < 0 || col2 >= board.size)
            return false;
        int dr = Math.abs(row1 - row2);
        int dc = Math.abs(col1 - col2);
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
    }
}