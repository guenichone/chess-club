package org.chesscorp.club.service;

import org.chesscorp.club.model.ChessGame;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ChessGameService {

    ChessGame createGame(Number whitePlayer, Number blackPlayer);

    ChessGame getGame(Number id);

    ChessGame move(ChessGame game, String pgnMove);

    List<ChessGame> searchGames(Number playerId);

    /**
     * Import a series of games from a PGN data stream.
     *
     * @param pgnStream the PGN stream which can contain either a single of multiple game files
     * @return the number of imported games
     * @throws IOException whenever stream reading fails
     */
    long batchImport(InputStream pgnStream) throws IOException;
}
