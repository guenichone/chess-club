package org.chesscorp.club.jobs;

import org.alcibiade.chess.model.ChessGameStatus;
import org.chesscorp.club.model.people.RobotPlayer;
import org.chesscorp.club.persistence.ChessGameRepository;
import org.chesscorp.club.service.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Re-start stale robot moves.
 */
@Component
public class RobotRecoveryJob {

    private Logger logger = LoggerFactory.getLogger(RobotRecoveryJob.class);

    private MessagingService messagingService;
    private ChessGameRepository chessGameRepository;

    @Autowired
    public RobotRecoveryJob(MessagingService messagingService, ChessGameRepository chessGameRepository) {
        this.messagingService = messagingService;
        this.chessGameRepository = chessGameRepository;
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 3600_000)
    @Transactional
    public void triggerRobotGameEvents() {
        chessGameRepository.findAllByStatus(ChessGameStatus.OPEN)
                .filter(g -> g.getNextPlayer() instanceof RobotPlayer)
                .forEach(g -> {
                    logger.debug("Triggering post-update job for game {}", g);
                    messagingService.notifyGameUpdated(g);
                });
    }

}
