# Chess Arena

A lightweight full-stack chess demo built with a Java backend and a browser-based frontend.

## Features
- Simple chess engine with turn tracking and basic pawn movement
- REST API for creating a game and applying moves
- Responsive web board with piece rendering

## Run locally
1. Open a terminal in this repository.
2. Compile and start the server:
   - javac -d target/classes src/main/java/com/chess/*.java
   - java -cp target/classes com.chess.ChessServer
3. Open http://localhost:8080 in your browser.

## Project structure
- src/main/java/com/chess/ChessGame.java - core game logic
- src/main/java/com/chess/ChessServer.java - HTTP server and API
- web/index.html - browser UI
