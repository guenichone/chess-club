package org.chesscorp.club.service;

import org.chesscorp.club.model.game.ChessGame;
import org.chesscorp.club.model.people.Player;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ChessGameService {

    ChessGame createGame(Number whitePlayer, Number blackPlayer);

    ChessGame getGame(Number id);

    ChessGame move(ChessGame game, String pgnMove);

    /**
     * Search for games.
     *
     * @param playerId identifier of a player that is search both as white or black
     * @param open     if null, return all games, otherwise return games which are open/ended based on this flag
     * @return all the matching games
     */
    List<ChessGame> searchGames(Number playerId, Boolean open);

    /**
     * Import a series of games from a PGN data stream.
     *
     * @param pgnStream the PGN stream which can contain either a single of multiple game files
     * @return the number of imported games
     * @throws IOException whenever stream reading fails
     */
    long batchImport(InputStream pgnStream) throws IOException;

    /**
     * Resign on an existing game. It is accounted as a loss if a complete turn has been played.
     *
     * @param game   the target game.
     * @param player the player resigning.
     * @return the updated game model.
     */
    ChessGame resign(ChessGame game, Player player);
}
