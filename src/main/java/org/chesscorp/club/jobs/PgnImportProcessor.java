package org.chesscorp.club.jobs;

import org.chesscorp.club.monitoring.PerformanceMonitor;
import org.chesscorp.club.service.ChessGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Import a single PGN file in the database.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PgnImportProcessor {

    private Logger logger = LoggerFactory.getLogger(PgnImportProcessor.class);

    @Autowired
    private ChessGameService chessGameService;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    public File process(File file) {
        logger.info("Importing games from " + file);

        try (InputStream stream = new FileInputStream(file)) {
            performanceMonitor.mark();
            long importCount = chessGameService.batchImport(file.getName(), stream);
            performanceMonitor.register("PgnImportProcessor", "import", importCount, "game");
            logger.info("Imported {} game(s) from {}", importCount, file);
        } catch (IOException e) {
            throw new IllegalStateException("File processing failed on " + file, e);
        }

        return file;
    }
}
