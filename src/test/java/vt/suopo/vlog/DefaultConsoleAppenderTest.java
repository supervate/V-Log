package vt.suopo.vlog;

import org.junit.jupiter.api.*;
import vt.suopo.vlog.appender.Appender;
import vt.suopo.vlog.appender.AppenderCombiner;
import vt.suopo.vlog.appender.DefaultFileAppender;
import vt.suopo.vlog.appender.DefaultPrintStreamAppender;
import vt.suopo.vlog.common.Constants;
import vt.suopo.vlog.common.ReflectUtils;
import vt.suopo.vlog.event.Level;
import vt.suopo.vlog.event.LogEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import static vt.suopo.vlog.common.ReflectUtils.getFieldValue;
import static vt.suopo.vlog.event.Level.INFO;

/**
 * 功能：日志测试
 *
 * @author suopovate
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
