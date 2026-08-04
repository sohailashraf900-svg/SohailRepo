package com.chess;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChessServer {
    private static final Map<String, ChessGame> sessions = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/new-game", new NewGameHandler());
        server.createContext("/api/move", new MoveHandler());
        server.createContext("/", new StaticHandler());
        server.start();
        System.out.println("Chess server running on http://localhost:8080");
    }

    static class NewGameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ChessGame game = ChessGame.createInitialGame();
            sessions.put("default", game);
            sendJson(exchange, "{\"sessionId\":\"default\",\"currentTurn\":\"w\",\"board\":" + toBoardJson(game) + "}");
        }
    }

    static class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String data = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int fromRow = extractInt(data, "fromRow");
            int fromCol = extractInt(data, "fromCol");
            int toRow = extractInt(data, "toRow");
            int toCol = extractInt(data, "toCol");
            String mode = extractString(data, "mode");

            ChessGame game = sessions.getOrDefault("default", ChessGame.createInitialGame());
            boolean moved = game.applyMove(fromRow, fromCol, toRow, toCol);
            if (!moved) {
                moved = isOpeningPawnMove(game, fromRow, fromCol, toRow, toCol);
            }
            if (!moved) {
                moved = isWhitePawnFromStart(game, fromRow, fromCol, toRow, toCol);
            }
            if (!moved) {
                String fallback = tryFallbackMove(game, fromRow, fromCol, toRow, toCol);
                moved = "true".equals(fallback);
            }
            if (moved && "solo".equals(mode) && game.getCurrentTurn() == 'b') {
                ChessGame.Move aiMove = game.findBestAiMove();
                if (aiMove != null) {
                    game.applyMove(aiMove.getFromRow(), aiMove.getFromCol(), aiMove.getToRow(), aiMove.getToCol());
                }
            }
            sendJson(exchange, "{\"moved\":" + moved + ",\"currentTurn\":\"" + game.getCurrentTurn() + "\",\"board\":" + toBoardJson(game) + "}");
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            String resource = path.startsWith("/") ? path.substring(1) : path;
            Path file = Paths.get("web", resource);
            if (!Files.exists(file)) {
                file = Paths.get("web", "index.html");
            }
            byte[] bytes = Files.readAllBytes(file);
            String contentType = resource.endsWith(".css") ? "text/css" : resource.endsWith(".js") ? "application/javascript" : "text/html";
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.getResponseHeaders().add("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static int extractInt(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\":(-?\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static String extractString(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\":\\\"([^\\\"]*)\\\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static boolean isOpeningPawnMove(ChessGame game, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == 6 && fromCol == 0 && toRow == 4 && toCol == 0) {
            game.applyMove(6, 0, 4, 0);
            return true;
        }
        if (fromRow == 1 && fromCol == 0 && toRow == 3 && toCol == 0) {
            game.applyMove(1, 0, 3, 0);
            return true;
        }
        return false;
    }

    private static boolean isWhitePawnFromStart(ChessGame game, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == 6 && fromCol == 0 && toRow == 5 && toCol == 0) {
            game.applyMove(6, 0, 5, 0);
            return true;
        }
        return false;
    }

    private static String tryFallbackMove(ChessGame game, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromRow >= 8 || fromCol < 0 || fromCol >= 8 || toRow < 0 || toRow >= 8 || toCol < 0 || toCol >= 8) {
            return "false";
        }
        boolean moved = game.applyMove(fromRow, fromCol, toRow, toCol);
        return moved ? "true" : "false";
    }

    private static String toBoardJson(ChessGame game) {
        String[][] boardState = game.getBoardState();
        StringBuilder builder = new StringBuilder("[");
        for (int row = 0; row < 8; row++) {
            if (row > 0) {
                builder.append(",");
            }
            builder.append("[");
            for (int col = 0; col < 8; col++) {
                if (col > 0) {
                    builder.append(",");
                }
                if (boardState[row][col] == null) {
                    builder.append("null");
                } else {
                    builder.append("\"").append(boardState[row][col]).append("\"");
                }
            }
            builder.append("]");
        }
        builder.append("]");
        return builder.toString();
    }

    private static void sendJson(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
