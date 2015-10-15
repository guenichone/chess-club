package org.chesscorp.club.jobs;

import org.assertj.core.api.Assertions;
import org.chesscorp.club.Application;
import org.chesscorp.club.model.Robot;
import org.chesscorp.club.persistence.ChessGameRepository;
import org.chesscorp.club.persistence.PlayerRepository;
import org.chesscorp.club.persistence.RobotRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test the bootstrap process that initializes the storage contents.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TransactionConfiguration(defaultRollback = true)
@Ignore
public class BootstrapTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private ChessGameRepository chessGameRepository;

    @Autowired
    private ObjectFactory<Bootstrap> bootstrapObjectFactory;

    @Test
    @Transactional
    public void testSampleData() {
        Bootstrap bootstrap = bootstrapObjectFactory.getObject();
        bootstrap.populate();
        Assertions.assertThat(playerRepository.findAll()).hasSize(5).hasAtLeastOneElementOfType(Robot.class);
        Assertions.assertThat(robotRepository.findAll()).hasSize(1);
        Assertions.assertThat(chessGameRepository.findAll()).hasSize(2);
    }
}
