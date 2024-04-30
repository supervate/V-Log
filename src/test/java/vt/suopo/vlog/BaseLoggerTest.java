package vt.suopo.vlog;

import org.junit.jupiter.api.*;
import vt.suopo.vlog.appender.*;
import vt.suopo.vlog.common.Constants;
import vt.suopo.vlog.common.ReflectUtils;
import vt.suopo.vlog.common.SystemUtils;
import vt.suopo.vlog.event.Level;
import vt.suopo.vlog.event.LogEvent;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import static vt.suopo.vlog.common.FileUtils.deleteDirectoryRecursively;
import static vt.suopo.vlog.common.ReflectUtils.getField;
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
public class BaseLoggerTest {

    protected static final Level TEST_DEFAULT_LEVEL = INFO;
    protected static final Path LOG_DIR = SystemUtils.getSysTempDir().resolve("test").resolve(BaseLoggerTest.class.getSimpleName());

    @BeforeAll
    public static void beforeAll() throws IOException {
        deleteDirectoryRecursively(LOG_DIR);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        Files.createDirectories(LOG_DIR);
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteDirectoryRecursively(LOG_DIR);
    }

    public void benchMark() throws InterruptedException, IllegalAccessException {
        Logger byName = LoggerFactory.logger(BaseLoggerTest.class.getCanonicalName());
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            byName.warn("benchMark");
        }
        waitingForLoggerFactoryAsyncAppend();
        long endTime = System.currentTimeMillis();
        System.out.println("toast: " + (endTime - startTime));
    }

    protected static Logger newLogger(String name, Appender<?> appender, Level level) {
        try {
            Constructor<Logger> declaredConstructor = Logger.class.getDeclaredConstructor(
                String.class,
                Appender.class,
                Level.class
            );
            declaredConstructor.setAccessible(true);
            return declaredConstructor
                .newInstance(name, appender, level);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void waitingForLoggerFactoryAsyncAppend() throws IllegalAccessException, InterruptedException {
        List<Appender<?>> appenders = getFieldValue(
            getFieldValue(LoggerFactory.class, "APPENDER_COMBINER", true),
            "appenderList",
            true
        );
        boolean allEmpty = false;
        while (!allEmpty) {
            allEmpty = true;
            for (Appender<?> appender : Objects.requireNonNull(appenders)) {
                if (appender instanceof AsyncAppender) {
                    BlockingQueue<?> queue = (BlockingQueue<?>) Objects
                        .requireNonNull(getField(AsyncAppender.class, "queue", true))
                        .get(appender);
                    if (queue != null) {
                        allEmpty &= queue.isEmpty();
                    }
                }
            }
            if (allEmpty) {
                Thread.sleep(1);
            }
        }
    }

    protected static void waitingForAsyncAppend(AsyncAppender<?> asyncAppender) throws IllegalAccessException, InterruptedException {
        BlockingQueue<?> queue = (BlockingQueue<?>) Objects
            .requireNonNull(getField(AsyncAppender.class, "queue", true))
            .get(asyncAppender);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Thread.sleep(1);
            }
        }
        Thread.sleep(1);
    }

    protected static void makeLevelLogs(Logger logger) {
        for (Level level : Level.values()) {
            ReflectUtils
                .getMethodWrapper(logger, level.name().toLowerCase(), String.class, Object[].class)
                .invoke(level.name(), new Object[0]);
        }
    }

    protected static void checkLevelLogs(Logger logger, String[] logs, Level limitLevel) {
        int allowLevelNum = Level.ERROR.ordinal() - limitLevel.ordinal() + 1;
        List<Level> allowLevels = new ArrayList<>();
        for (Level level : Level.values()) {
            if (level.compareTo(limitLevel) >= 0) {
                allowLevels.add(level);
            }
        }
        Assertions.assertNotNull(logs);
        Assertions.assertEquals(allowLevelNum, logs.length);
        for (int i = 0; i < allowLevelNum; i++) {
            // [2024-04-24T16:13:15.027] [main] [INFO] [com.github.log.LoggerTest] - INFO
            System.out.printf("check log content pass: %s%n", logs[i]);
            String[] logItems = logs[i].split(Constants.SPACE);
            Level level = allowLevels.get(i);
            Assertions.assertEquals(logItems[1], buildItem(Thread.currentThread().getName()));
            Assertions.assertEquals(logItems[2], buildItem(level.name()));
            Assertions.assertEquals(logItems[3], buildItem(logger.getName()));
            Assertions.assertEquals(logItems[5], level.name());
        }
    }

    protected static String buildItem(String item) {
        return Constants.LEFT_MIDDLE_BRACKET + item + Constants.RIGHT_MIDDLE_BRACKET;
    }

    protected static void printDirectory(Path rollingFileDir) throws IOException {
        Files
            .list(rollingFileDir)
            .sorted(Comparator.comparing(Path::getFileName))
            .forEach(System.out::println);
    }

    protected static LogEvent buildLogEvent(Logger logger, String message, Object[] args) {
        return ReflectUtils
            .getMethodWrapper(logger, "buildLogEvent", true, Level.class, String.class, Object[].class)
            .invoke(INFO, message, args);
    }

}
