package com.chess;

import java.util.List;

public class ChessEngineTest {
    public static void main(String[] args) {
        ChessGame game = ChessGame.createInitialGame();
        check(game.getCurrentTurn() == 'w', "White should start");
        check(game.getBoard()[6][0].getType() == 'P', "White pawn should start on row 6");
        check(game.getBoard()[1][0].getType() == 'P', "Black pawn should start on row 1");

        boolean moved = game.applyMove(6, 0, 4, 0);
        check(moved, "White pawn should be able to move forward");
        check(game.getBoard()[4][0].getType() == 'P', "Pawn should move to target square");
        check(game.getCurrentTurn() == 'b', "Turn should switch to black after white move");

        ChessGame openingGame = ChessGame.createInitialGame();
        boolean twoStepMove = openingGame.applyMove(6, 0, 4, 0);
        check(twoStepMove, "White pawn should be able to move two squares on its first move");
        check(openingGame.getBoard()[4][0].getType() == 'P', "White pawn should land on the two-step destination");
        check(openingGame.getBoard()[5][0] == null, "The intermediate square should remain empty after a two-step pawn move");

        ChessGame rookGame = ChessGame.createInitialGame();
        check(rookGame.applyMove(7, 0, 7, 3), "White rook should move horizontally when path is clear");

        ChessGame knightGame = ChessGame.createInitialGame();
        check(knightGame.applyMove(7, 1, 5, 2), "White knight should move in an L-shape");

        ChessGame bishopGame = ChessGame.createInitialGame();
        check(bishopGame.applyMove(7, 2, 5, 4), "White bishop should move diagonally when path is clear");

        ChessGame queenGame = ChessGame.createInitialGame();
        check(queenGame.applyMove(7, 3, 7, 6), "White queen should move horizontally when path is clear");

        ChessGame kingGame = ChessGame.createInitialGame();
        check(kingGame.applyMove(7, 4, 6, 4), "White king should move one square");

        List<ChessGame.Move> moves = game.getLegalMovesForPiece(1, 0);
        check(moves.size() == 2, "Black pawn should have two legal opening moves");
        check(moves.stream().anyMatch(move -> move.getToRow() == 2 && move.getToCol() == 0), "Black pawn should have a one-step move");
        check(moves.stream().anyMatch(move -> move.getToRow() == 3 && move.getToCol() == 0), "Black pawn should have a two-step move");

        System.out.println("Chess engine tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
