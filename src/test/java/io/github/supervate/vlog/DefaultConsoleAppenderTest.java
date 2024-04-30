package io.github.supervate.vlog;

import org.junit.jupiter.api.*;
import io.github.supervate.vlog.appender.DefaultPrintStreamAppender;
import io.github.supervate.vlog.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static io.github.supervate.vlog.common.ReflectUtils.getFieldValue;

/**
 * 功能：日志测试
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings({ "resource", "BusyWait" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultConsoleAppenderTest extends BaseLoggerTest{

    @Test
    public void logConsole() throws IOException, InterruptedException, IllegalAccessException {
        for (Level limitLevel : Level.values()) {
            ByteArrayOutputStream logCollectStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(logCollectStream);
            DefaultPrintStreamAppender defaultPrintStreamAppender = new DefaultPrintStreamAppender(
                new DefaultLayout(),
                printStream,
                printStream
            );
            defaultPrintStreamAppender.start();
            Logger logger = newLogger(DefaultConsoleAppenderTest.class.getCanonicalName(), defaultPrintStreamAppender, limitLevel);
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
