package io.github.supervate.vlog;

import io.github.supervate.vlog.appender.DefaultPrintStreamAppender;
import io.github.supervate.vlog.event.Level;
import io.github.supervate.vlog.layout.DefaultLineLayout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * 功能：日志测试
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultConsoleAppenderTest extends BaseLoggerTest {

    @Test
    public void logConsole() throws IOException, InterruptedException, IllegalAccessException {
        for (Level limitLevel : Level.values()) {
            ByteArrayOutputStream logCollectStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(logCollectStream);
            DefaultPrintStreamAppender defaultPrintStreamAppender = new DefaultPrintStreamAppender(
                new DefaultLineLayout(),
                printStream,
                printStream
            );
            defaultPrintStreamAppender.start();
            Logger logger = newLogger(
                DefaultConsoleAppenderTest.class.getCanonicalName(),
                defaultPrintStreamAppender,
                limitLevel
            );
            // test every limit level log print
            Assertions.assertNotNull(logger);

            // make all level's log
            makeLevelLogs(logger);
            // waiting for log collect,its async write.
            waitingForAsyncAppend(defaultPrintStreamAppender);
            // check console log
            checkLevelLogs(
                logger,
                logCollectStream.toString(StandardCharsets.UTF_8.name()).split(System.lineSeparator()),
                logger.getLevel()
            );

            defaultPrintStreamAppender.stop();
        }
    }

}
