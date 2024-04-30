package io.github.supervate.vlog;

import io.github.supervate.vlog.appender.Appender;
import io.github.supervate.vlog.appender.AppenderCombiner;
import io.github.supervate.vlog.appender.DefaultFileAppender;
import io.github.supervate.vlog.appender.DefaultPrintStreamAppender;
import org.junit.jupiter.api.*;
import io.github.supervate.vlog.common.Constants;
import io.github.supervate.vlog.event.LogEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import static io.github.supervate.vlog.common.ReflectUtils.getFieldValue;
import static io.github.supervate.vlog.event.Level.INFO;

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
public class LoggerTest extends BaseLoggerTest{

    @Test
    public void defaultLayout() {
        DefaultLayout defaultLayout = new DefaultLayout();
        Logger logger = newLogger(LoggerTest.class.getCanonicalName(), null, INFO);
        LogEvent logEvent = buildLogEvent(logger, "test", new Object[0]);
        String timeStr = buildItem(
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(logEvent.getEventTime()), ZoneId.systemDefault())
                .toString()
        );
        // [2024-04-27T17:37:22.166] [main] [INFO] [com.github.logger.LoggerTest] - test
        Assertions.assertEquals(
            timeStr + " [main] [INFO] [" + LoggerTest.class.getCanonicalName() + "] - test" + System.lineSeparator(),
            defaultLayout.format(logEvent)
        );

        logEvent = buildLogEvent(logger, "test%s%s%s%d", new Object[]{ "-", "format", "-", 1 });
        timeStr = buildItem(
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(logEvent.getEventTime()), ZoneId.systemDefault())
                .toString()
        );
        Assertions.assertEquals(
            timeStr + " [main] [INFO] [" + LoggerTest.class.getCanonicalName() + "] - test-format-1" + System.lineSeparator(),
            defaultLayout.format(logEvent)
        );
    }

    @Test
    public void getLogger() {
        // test systemProperty set
        System.setProperty(Constants.SYSTEM_PROPERTY_LOG_LEVEL, TEST_DEFAULT_LEVEL.name());
        System.setProperty(Constants.SYSTEM_PROPERTY_LOG_DIR, LOG_DIR.toString());

        Logger byName = LoggerFactory.logger(LoggerTest.class.getCanonicalName());
        Logger byClass = LoggerFactory.logger(LoggerTest.class);

        Assertions.assertSame(byName, byClass);

        Assertions.assertNotNull(byName);
        Assertions.assertEquals(INFO, byName.getLevel());
        Assertions.assertEquals(LoggerTest.class.getCanonicalName(), byName.getName());
        AppenderCombiner<LogEvent> appenderCombiner = getFieldValue(byName, "appender", true);
        Assertions.assertSame(
            getFieldValue(LoggerFactory.class, "APPENDER_COMBINER", true),
            appenderCombiner
        );
        boolean defaultPrintStreamAppendExists = false;
        boolean defaultFileAppendExists = false;
        List<Appender<?>> appenderList = getFieldValue(appenderCombiner, "appenderList", true);
        for (Appender<?> appender : Objects.requireNonNull(appenderList)) {
            if (appender instanceof DefaultPrintStreamAppender) {
                defaultPrintStreamAppendExists = true;
            }
            if (appender instanceof DefaultFileAppender) {
                defaultFileAppendExists = true;
            }
        }
        Assertions.assertTrue(defaultPrintStreamAppendExists);
        Assertions.assertTrue(defaultFileAppendExists);
    }

}
