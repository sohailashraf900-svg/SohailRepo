package com.chess;

import java.util.ArrayList;
import java.util.List;

public class ChessGame {
    public static class Move {
        private final int fromRow;
        private final int fromCol;
        private final int toRow;
        private final int toCol;

        public Move(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
        }

        public int getFromRow() { return fromRow; }
        public int getFromCol() { return fromCol; }
        public int getToRow() { return toRow; }
        public int getToCol() { return toCol; }
    }

    private final Piece[][] board = new Piece[8][8];
    private char currentTurn = 'w';

    public static ChessGame createInitialGame() {
        ChessGame game = new ChessGame();
        for (int col = 0; col < 8; col++) {
            game.board[6][col] = new Piece('P', 'w');
            game.board[1][col] = new Piece('P', 'b');
        }
        game.board[7][0] = new Piece('R', 'w');
        game.board[7][7] = new Piece('R', 'w');
        game.board[7][1] = new Piece('N', 'w');
        game.board[7][6] = new Piece('N', 'w');
        game.board[7][2] = new Piece('B', 'w');
        game.board[7][5] = new Piece('B', 'w');
        game.board[7][3] = new Piece('Q', 'w');
        game.board[7][4] = new Piece('K', 'w');

        game.board[0][0] = new Piece('R', 'b');
        game.board[0][7] = new Piece('R', 'b');
        game.board[0][1] = new Piece('N', 'b');
        game.board[0][6] = new Piece('N', 'b');
        game.board[0][2] = new Piece('B', 'b');
        game.board[0][5] = new Piece('B', 'b');
        game.board[0][3] = new Piece('Q', 'b');
        game.board[0][4] = new Piece('K', 'b');
        return game;
    }

    public Piece[][] getBoard() {
        return board;
    }

    public char getCurrentTurn() {
        return currentTurn;
    }

    public boolean applyMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isWithinBoard(fromRow, fromCol) || !isWithinBoard(toRow, toCol)) {
            return false;
        }

        Piece piece = board[fromRow][fromCol];
        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }
        if (!isLegalMove(piece, fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        currentTurn = currentTurn == 'w' ? 'b' : 'w';
        return true;
    }

    private boolean isLegalMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (!isWithinBoard(toRow, toCol)) {
            return false;
        }

        Piece target = board[toRow][toCol];
        if (target != null && target.getColor() == piece.getColor()) {
            return false;
        }

        switch (piece.getType()) {
            case 'P':
                return isLegalPawnMove(piece, fromRow, fromCol, toRow, toCol);
            case 'R':
                return (fromRow == toRow || fromCol == toCol) && isPathClear(fromRow, fromCol, toRow, toCol);
            case 'N':
                return (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 1)
                    || (Math.abs(fromRow - toRow) == 1 && Math.abs(fromCol - toCol) == 2);
            case 'B':
                return Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol) && isPathClear(fromRow, fromCol, toRow, toCol);
            case 'Q':
                return (fromRow == toRow || fromCol == toCol || Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol))
                    && isPathClear(fromRow, fromCol, toRow, toCol);
            case 'K':
                return Math.max(Math.abs(fromRow - toRow), Math.abs(fromCol - toCol)) == 1;
            default:
                return false;
        }
    }

    private boolean isLegalPawnMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        int direction = piece.getColor() == 'w' ? -1 : 1;
        int startingRow = piece.getColor() == 'w' ? 6 : 1;
        int oneStepRow = fromRow + direction;
        int twoStepRow = fromRow + 2 * direction;

        if (fromCol == toCol && board[toRow][toCol] == null) {
            if (toRow == oneStepRow) {
                return true;
            }
            return fromRow == startingRow && toRow == twoStepRow && board[oneStepRow][toCol] == null;
        }

        if (Math.abs(toCol - fromCol) == 1 && toRow == oneStepRow) {
            Piece target = board[toRow][toCol];
            return target != null && target.getColor() != piece.getColor();
        }
        return false;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int row = fromRow + rowStep;
        int col = fromCol + colStep;

        while (row != toRow || col != toCol) {
            if (board[row][col] != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    private boolean isWithinBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public List<Move> getLegalMovesForPiece(int row, int col) {
        Piece piece = board[row][col];
        if (piece == null || piece.getColor() != currentTurn) {
            return List.of();
        }

        List<Move> moves = new ArrayList<>();
        for (int targetRow = 0; targetRow < 8; targetRow++) {
            for (int targetCol = 0; targetCol < 8; targetCol++) {
                if (targetRow == row && targetCol == col) {
                    continue;
                }
                if (isLegalMove(piece, row, col, targetRow, targetCol)) {
                    moves.add(new Move(row, col, targetRow, targetCol));
                }
            }
        }
        return moves;
    }

    public List<Move> getLegalMovesForCurrentPlayer() {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                moves.addAll(getLegalMovesForPiece(row, col));
            }
        }
        return moves;
    }

    public Move findBestAiMove() {
        List<Move> moves = getLegalMovesForCurrentPlayer();
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(0);
    }

    public String[][] getBoardState() {
        String[][] state = new String[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    state[row][col] = Character.toString(piece.getColor()) + piece.getType();
                }
            }
        }
        return state;
    }
}

class Piece {
    private final char type;
    private final char color;

    Piece(char type, char color) {
        this.type = type;
        this.color = color;
    }

    public char getType() {
        return type;
    }

    public char getColor() {
        return color;
    }
}
