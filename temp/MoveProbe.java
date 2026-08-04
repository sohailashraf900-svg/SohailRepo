package com.chess;

public class MoveProbe {
    public static void main(String[] args) {
        ChessGame game = ChessGame.createInitialGame();
        boolean moved = game.applyMove(6, 0, 4, 0);
        System.out.println("moved=" + moved);
        System.out.println("square=" + (game.getBoard()[4][0] != null));
        System.out.println("turn=" + game.getCurrentTurn());
    }
}
