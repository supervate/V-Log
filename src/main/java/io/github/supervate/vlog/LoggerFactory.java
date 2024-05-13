package io.github.supervate.vlog;

import io.github.supervate.vlog.appender.AppenderCombiner;
import io.github.supervate.vlog.appender.DefaultFileAppender;
import io.github.supervate.vlog.appender.DefaultPrintStreamAppender;
import io.github.supervate.vlog.common.Constants;
import io.github.supervate.vlog.common.SystemUtils;
import io.github.supervate.vlog.event.Level;
import io.github.supervate.vlog.event.LogEvent;
import io.github.supervate.vlog.layout.DefaultLineLayout;
import io.github.supervate.vlog.layout.Layout;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.supervate.vlog.common.Constants.SYSTEM_PROPERTY_LOG_LAYOUT_IMPL;
import static io.github.supervate.vlog.common.SystemUtils.getSysProperty;

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
 * <li>
 * 5. vt.vlog.layout.impl 日志格式输出实现类,默认是DefaultLineLayout,支持用户自定义.
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class LoggerFactory {

    private static final ConcurrentHashMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    private static AppenderCombiner<LogEvent> APPENDER_COMBINER;

    volatile private static Level LEVEL;

    static {
        init();
    }

    private static void init() {
        // appender set
        APPENDER_COMBINER = new AppenderCombiner<>();
        DefaultPrintStreamAppender defaultPrintStreamAppender = new DefaultPrintStreamAppender(
            buildLayout(),
            System.out,
            System.err
        );
        defaultPrintStreamAppender.start();
        APPENDER_COMBINER.addAppender(defaultPrintStreamAppender);
        SystemUtils
            .getSysPropertyPath(Constants.SYSTEM_PROPERTY_LOG_DIR)
            .ifPresent(path -> {
                DefaultFileAppender defaultFileAppender = new DefaultFileAppender(
                    buildLayout(),
                    path,
                    SystemUtils
                        .getSysPropertyInteger(Constants.SYSTEM_PROPERTY_LOG_FILE_RETENTION)
                        .orElse(Constants.DEFAULT_LOG_FILE_RETENTION),
                    SystemUtils
                        .getSysPropertyInteger(Constants.SYSTEM_PROPERTY_LOG_FILE_SIZE)
                        .orElse(Constants.DEFAULT_LOG_FILE_SIZE)
                );
                defaultFileAppender.start();
                APPENDER_COMBINER.addAppender(defaultFileAppender);
            });
        APPENDER_COMBINER.start();
        // level set
        LEVEL = getLevelConfig();
    }

    private static Layout<LogEvent> buildLayout() {
        return getSysProperty(SYSTEM_PROPERTY_LOG_LAYOUT_IMPL).map(LoggerFactory::loadLayout).orElseGet(DefaultLineLayout::new);
    }

    @SuppressWarnings("unchecked")
    private static Layout<LogEvent> loadLayout(String className) {
        Class<?> layoutClazz;
        Exception loadLayoutException = null;
        try {
            layoutClazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            if (Layout.class.isAssignableFrom(layoutClazz)) {
                for (Type genericInterface : layoutClazz.getGenericInterfaces()) {
                    if (genericInterface instanceof ParameterizedType && ((ParameterizedType) genericInterface)
                        .getRawType()
                        .equals(Layout.class)) {
                        Type[] actualTypeArguments = ((ParameterizedType) genericInterface).getActualTypeArguments();
                        for (Type actualTypeArgument : actualTypeArguments) {
                            if (actualTypeArgument.getTypeName().equals(LogEvent.class.getTypeName())) {
                                return (Layout<LogEvent>) layoutClazz.getConstructor().newInstance();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            loadLayoutException = e;
        }
        throw loadLayoutException != null
              ? new IllegalArgumentException("Invalid layout class: " + className, loadLayoutException)
              : new IllegalArgumentException("Invalid layout class: " + className);
    }

    private static Level getLevelConfig() {
        return Optional
            .ofNullable(System.getProperty(Constants.SYSTEM_PROPERTY_LOG_LEVEL))
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
