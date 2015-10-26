package org.chesscorp.club.controllers;

import org.assertj.core.api.Assertions;
import org.chesscorp.club.Application;
import org.chesscorp.club.exception.ChessException;
import org.chesscorp.club.model.ChessGame;
import org.chesscorp.club.model.ChessMove;
import org.chesscorp.club.model.Player;
import org.chesscorp.club.persistence.PlayerRepository;
import org.chesscorp.club.service.AuthenticationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TransactionConfiguration(defaultRollback = true)
public class ChessGameControllerTest {
    private Logger logger = LoggerFactory.getLogger(ChessGameControllerTest.class);

    @Autowired
    private ChessGameController chessGameController;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void testEmptyMvcController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(chessGameController).build();

        mockMvc.perform(
                post("/api/chess/game/search").param("playerId", "777")
        ).andExpect(
                status().is2xxSuccessful()
        ).andExpect(
                jsonPath("$", hasSize(0))
        );
    }

    @Test
    @Transactional
    public void testBasicGame() throws Exception {
        authenticationService.signup("a@b.c", "pwd", "Alcibiade");
        String alcibiadeToken = authenticationService.signin("a@b.c", "pwd");
        Player alcibiade = authenticationService.getSession(alcibiadeToken).getAccount().getPlayer();


        authenticationService.signup("b@b.c", "pwd", "Bob");
        String bobToken = authenticationService.signin("b@b.c", "pwd");
        Player bob = authenticationService.getSession(bobToken).getAccount().getPlayer();

        /*
         * Game creation.
         */

        ChessGame game1 = chessGameController.createGame(alcibiadeToken, alcibiade.getId(), bob.getId());
        Assertions.assertThat(game1.getWhitePlayer()).isEqualToComparingFieldByField(alcibiade);
        Assertions.assertThat(game1.getBlackPlayer()).isEqualToComparingFieldByField(bob);
        Assertions.assertThat(game1.getId().longValue()).isGreaterThan(0L);
        Assertions.assertThat(game1.getStartDate()).isInThePast();

        /*
         * Game fetch
         */

        ChessGame game2 = chessGameController.getGame(game1.getId());
        Assertions.assertThat(game2).isEqualToComparingFieldByField(game1);

        /*
         * Game moves
         */

        ChessGame game3 = chessGameController.postMove(alcibiadeToken, game1.getId(), "e4");
        Assertions.assertThat(game3.getMoves()).extracting(ChessMove::getPgn).containsExactly("e4");

        ChessGame game4 = chessGameController.postMove(bobToken, game1.getId(), "e5");
        Assertions.assertThat(game4.getMoves()).extracting(ChessMove::getPgn).containsExactly("e4", "e5");

        List<ChessGame> games = chessGameController.search(alcibiade.getId(), true);
        Assertions.assertThat(games).containsExactly(game1);

        Assertions.assertThat(chessGameController.search(alcibiade.getId(), false)).isEmpty();
        Assertions.assertThat(chessGameController.search(alcibiade.getId(), null)).isEqualTo(games);

        /*
         * Search games from the controller itself.
         */
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(chessGameController).build();

        mockMvc.perform(
                post("/api/chess/game/search").param("playerId", alcibiade.getId().toString())
        ).andExpect(
                status().is2xxSuccessful()
        ).andExpect(
                jsonPath("$", hasSize(1))
        );
    }

    @Test(expected = ChessException.class)
    @Transactional
    public void testRefuseThirdPartyCreation() {
        authenticationService.signup("a@b.c", "pwd", "Alcibiade");
        String alcibiadeToken = authenticationService.signin("a@b.c", "pwd");
        Player alcibiade = authenticationService.getSession(alcibiadeToken).getAccount().getPlayer();

        authenticationService.signup("b@b.c", "pwd", "Bob");
        String bobToken = authenticationService.signin("b@b.c", "pwd");
        Player bob = authenticationService.getSession(bobToken).getAccount().getPlayer();

        authenticationService.signup("c@b.c", "pwd", "Charlie");
        String charlieToken = authenticationService.signin("c@b.c", "pwd");

        ChessGame game1 = chessGameController.createGame(charlieToken, alcibiade.getId(), bob.getId());
        Assertions.assertThat(game1.getWhitePlayer()).isEqualToComparingFieldByField(alcibiade);
        Assertions.assertThat(game1.getBlackPlayer()).isEqualToComparingFieldByField(bob);
        Assertions.assertThat(game1.getId().longValue()).isGreaterThan(0L);
        Assertions.assertThat(game1.getStartDate()).isInThePast();

        ChessGame game2 = chessGameController.getGame(game1.getId());
        Assertions.assertThat(game2).isEqualToComparingFieldByField(game1);
    }

    @Test(expected = ChessException.class)
    @Transactional
    public void testRefuseMove() {
        authenticationService.signup("a@b.c", "pwd", "Alcibiade");
        String alcibiadeToken = authenticationService.signin("a@b.c", "pwd");
        Player alcibiade = authenticationService.getSession(alcibiadeToken).getAccount().getPlayer();

        authenticationService.signup("b@b.c", "pwd", "Bob");
        String bobToken = authenticationService.signin("b@b.c", "pwd");
        Player bob = authenticationService.getSession(bobToken).getAccount().getPlayer();

        ChessGame game1 = chessGameController.createGame(alcibiadeToken, alcibiade.getId(), bob.getId());
        chessGameController.postMove(alcibiadeToken, game1.getId(), "e4");
        // Refuse 2nd move from same player
        chessGameController.postMove(alcibiadeToken, game1.getId(), "e5");
    }

}
