package vt.suopo.vlog;

import vt.suopo.vlog.appender.AppenderCombiner;
import vt.suopo.vlog.appender.DefaultFileAppender;
import vt.suopo.vlog.appender.DefaultPrintStreamAppender;
import vt.suopo.vlog.common.SystemUtils;
import vt.suopo.vlog.event.Level;
import vt.suopo.vlog.event.LogEvent;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static vt.suopo.vlog.common.Constants.*;

/**
 * 日志工厂
 * <p>
 * 暂提供如下配置(通过SystemProperty配置):
 * <li>
 * 1. vt.vlog.dir 产生的日志文件存放目录
 * <li>
 * 2. vt.vlog.level 产生的日志的最低级别,大于对应级别的日志才会被打印.
 * <li>
 * 3. vt.vlog.file.retention 产生的日志文件存放时间,保留多少天,默认为7天.(单位天)
 * <li>
 * 4. vt.vlog.file.size 产生的日志文件大小,超过多少字节后,产生新的文件,默认为0,不限制.(单位字节)
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class LoggerFactory {

    private static final ConcurrentHashMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    private static final AppenderCombiner<LogEvent> APPENDER_COMBINER;

    volatile private static Level LEVEL;

    static {
        // appender set
        APPENDER_COMBINER = new AppenderCombiner<>();
        DefaultPrintStreamAppender defaultPrintStreamAppender = new DefaultPrintStreamAppender(
            new DefaultLayout(),
            System.out,
            System.err
        );
        defaultPrintStreamAppender.start();
        APPENDER_COMBINER.addAppender(defaultPrintStreamAppender);
        SystemUtils
            .getSysPropertyPath(SYSTEM_PROPERTY_LOG_DIR)
            .ifPresent(path -> {
                DefaultFileAppender defaultFileAppender = new DefaultFileAppender(
                    new DefaultLayout(),
                    path,
                    SystemUtils
                        .getSysPropertyInteger(SYSTEM_PROPERTY_LOG_FILE_RETENTION)
                        .orElse(DEFAULT_LOG_FILE_RETENTION),
                    SystemUtils
                        .getSysPropertyInteger(SYSTEM_PROPERTY_LOG_FILE_SIZE)
                        .orElse(DEFAULT_LOG_FILE_SIZE)
                );
                defaultFileAppender.start();
                APPENDER_COMBINER.addAppender(defaultFileAppender);
            });
        APPENDER_COMBINER.start();
        // level set
        LEVEL = getLevelConfig();
    }

    private static Level getLevelConfig() {
        return Optional
            .ofNullable(System.getProperty(SYSTEM_PROPERTY_LOG_LEVEL))
            .map(String::toUpperCase)
            .map(Level::valueOf)
            .orElse(Level.INFO);
    }

    public static Logger logger(Class<?> clazz) {
        return logger(clazz.getCanonicalName());
    }

    public static Logger logger(String name) {
        Logger logger = LOGGER_MAP.get(name);
        if (logger == null) {
            logger = new Logger(name, APPENDER_COMBINER, LEVEL);
            Logger preLogger = LOGGER_MAP.putIfAbsent(name, logger);
            logger = preLogger != null ? preLogger : logger;
        }
        return logger;
    }

}
