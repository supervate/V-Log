package io.github.supervate.vlog;

import io.github.supervate.vlog.appender.DefaultFileAppender;
import io.github.supervate.vlog.common.ReflectUtils;
import io.github.supervate.vlog.event.Level;
import io.github.supervate.vlog.layout.DefaultLineLayout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

import static io.github.supervate.vlog.event.Level.INFO;

/**
 * 功能：日志测试
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
@SuppressWarnings({ "resource" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultFileAppenderTest extends BaseLoggerTest {

    @Test
    public void logFile() throws IOException, InterruptedException, IllegalAccessException {
        Path logPath = LOG_DIR.resolve("logFile");
        Files.createDirectories(logPath);
        for (Level limitLevel : Level.values()) {
            // don't clean file
            DefaultFileAppender defaultFileAppender = new DefaultFileAppender(new DefaultLineLayout(), logPath, 0);
            defaultFileAppender.start();
            Logger logger = newLogger(DefaultFileAppenderTest.class.getCanonicalName(), defaultFileAppender, limitLevel);
            // test every limit level log print
            // make all level's log
            makeLevelLogs(logger);
            // waiting for log collect,its async write.
            waitingForAsyncAppend(defaultFileAppender);
            // check file log
            Path logFile = logPath.resolve(DefaultFileAppender.dateToLogFileName(LocalDateTime.now()));
            checkLevelLogs(
                logger, new String(Files.readAllBytes(logFile), StandardCharsets.UTF_8).split(System.lineSeparator()),
                logger.getLevel()
            );
            try (FileChannel fileChannel = FileChannel.open(logFile, StandardOpenOption.WRITE)) {
                // 将文件大小截断至0字节，相当于清空文件内容
                fileChannel.truncate(0);
            }
            defaultFileAppender.stop();
        }
    }

    @Test
    public void cleanExpiredFile() throws IOException, InterruptedException, ExecutionException {
        LocalDateTime now = LocalDateTime.now();
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path cleanExpiredFileDir = LOG_DIR.resolve("cleanExpiredFile");
        Files.createDirectories(cleanExpiredFileDir);
        int logFileRetentionDays = 7;
        DefaultFileAppender defaultFileAppender = new DefaultFileAppender(
            new DefaultLineLayout(),
            cleanExpiredFileDir,
            logFileRetentionDays
        );
        // make expired log file, here we needn't create file,because the appender will create it.
        Path unExpiredFile1 = cleanExpiredFileDir.resolve(DefaultFileAppender.dateToLogFileName(now));
        Path unExpiredFile2 = cleanExpiredFileDir
            .resolve(DefaultFileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays - 1)));
        Files.createFile(unExpiredFile2);
        Path expiredFile1 = cleanExpiredFileDir
            .resolve(DefaultFileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays)));
        Files.createFile(expiredFile1);
        Path expiredFile2 = cleanExpiredFileDir
            .resolve(DefaultFileAppender.dateToLogFileName(now.minusDays(logFileRetentionDays + 1)));
        Files.createFile(expiredFile2);

        System.out.println("before clean,log files: ");
        printDirectory(cleanExpiredFileDir);

        ScheduledFuture<?> future = ReflectUtils
            .getMethodWrapper(defaultFileAppender, "triggerCleanTask", true, LocalDateTime.class)
            .invoke(now);

        // waiting task finish
        future.get();

        System.out.println("after clean,log files: ");
        printDirectory(cleanExpiredFileDir);

        Assertions.assertTrue(Files.exists(unExpiredFile1));
        Assertions.assertTrue(Files.exists(unExpiredFile2));
        Assertions.assertFalse(Files.exists(expiredFile1));
        Assertions.assertFalse(Files.exists(expiredFile2));
    }

    @Test
    public void rollingFile() throws IOException, IllegalAccessException, InterruptedException {
        // because the LoggerFactory has DefaultFileAppender, it will be started that always scan and delete the log dir.
        Path rollingFileDir = LOG_DIR.resolve("rollingFile");
        Files.createDirectories(rollingFileDir);

        DefaultLineLayout defaultLayout = new DefaultLineLayout();
        DefaultFileAppender defaultFileAppender = new DefaultFileAppender(defaultLayout, rollingFileDir, 0, 0);
        defaultFileAppender.start();
        Logger logger = newLogger(DefaultFileAppenderTest.class.getCanonicalName(), defaultFileAppender, INFO);

        String message = "test";
        String log = defaultLayout.format(buildLogEvent(logger, message, new Object[0]));
        int logSize = log.getBytes().length;

        System.out.println("before files: ");
        printDirectory(rollingFileDir);
        // 刚好消息跟文件大小相同
        ReflectUtils.setFieldValue(defaultFileAppender, "logFileSizeBytes", logSize, true);
        logger.info(message);
        waitingForAsyncAppend(defaultFileAppender);
        Assertions.assertEquals(Files.list(rollingFileDir).count(), 1);
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        // 消息超过文件大小的情况,直接追加在当前文件后
        ReflectUtils.setFieldValue(defaultFileAppender, "logFileSizeBytes", logSize - 1, true);
        logger.info(message);
        waitingForAsyncAppend(defaultFileAppender);
        Assertions.assertEquals(Files.list(rollingFileDir).count(), 1);
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        // 文件剩余空间不足以填充当前消息 且 消息大小小于文件大小时 roll 到下一个文件
        ReflectUtils.setFieldValue(defaultFileAppender, "logFileSizeBytes", logSize * 2 + 1, true);
        for (int i = 0; i < 3; i++) {
            logger.info(message);
        }
        waitingForAsyncAppend(defaultFileAppender);
        Assertions.assertEquals(3, Files.list(rollingFileDir).count());
        System.out.println("after files: ");
        printDirectory(rollingFileDir);

        defaultFileAppender.stop();
    }

}
