package io.github.supervate.vlog;

import io.github.supervate.vlog.appender.Appender;
import io.github.supervate.vlog.appender.AppenderCombiner;
import io.github.supervate.vlog.appender.DefaultFileAppender;
import io.github.supervate.vlog.appender.DefaultPrintStreamAppender;
import io.github.supervate.vlog.common.Constants;
import io.github.supervate.vlog.common.ReflectUtils;
import io.github.supervate.vlog.event.LogEvent;
import io.github.supervate.vlog.layout.DefaultJsonLayout;
import io.github.supervate.vlog.layout.DefaultLineLayout;
import io.github.supervate.vlog.layout.Layout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoggerTest extends BaseLoggerTest {

    @Test
    public void formatMessage() {

        DefaultLineLayout defaultLayout = new DefaultLineLayout();

        checkFormatMessage(defaultLayout, "test", "test");
        checkFormatMessage(defaultLayout, "test{}", "test{}");
        checkFormatMessage(defaultLayout, "test{{}", "test{{}");
        checkFormatMessage(defaultLayout, "test{{}}", "test{{}}");
        checkFormatMessage(defaultLayout, "test{}{}", "test{}{}");

        checkFormatMessage(defaultLayout, "test", "test", "-");
        checkFormatMessage(defaultLayout, "test-", "test{}", "-");
        checkFormatMessage(defaultLayout, "test{-", "test{{}", "-");
        checkFormatMessage(defaultLayout, "test{-}", "test{{}}", "-");
        checkFormatMessage(defaultLayout, "test-{}", "test{}{}", "-");
        checkFormatMessage(defaultLayout, "test-1", "test{}{}", "-", 1);

    }

    private static void checkFormatMessage(Layout<?> layout, String expected, String message, Object... args) {
        String formated = layout.formatMessage(message, args);
        System.out.printf("message: %s, formated: %s, args: %s%n", message, formated, Arrays.toString(args));
        Assertions.assertEquals(
            expected,
            formated
        );
    }

    @Test
    public void defaultLayout() {
        DefaultLineLayout defaultLayout = new DefaultLineLayout();
        Logger logger = newLogger(LoggerTest.class.getCanonicalName(), null, INFO);
        LogEvent logEvent = buildLogEvent(logger, "test", new Object[0]);
        String timeStr = buildItem(
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(logEvent.getEventTime()), ZoneId.systemDefault())
                .toString()
        );
        // [2024-04-27T17:37:22.166] [main] [INFO] [com.github.logger.LoggerTest] - test
        String expected = timeStr + " [main] [INFO] [" + LoggerTest.class.getCanonicalName() + "] - test" + System.lineSeparator();
        String format = defaultLayout.format(logEvent);
        System.out.printf("expected: %sformat: %s %n", expected, format);
        Assertions.assertEquals(
            expected,
            format
        );
    }

    @Test
    public void jsonLayout() {
        String loggerName = LoggerTest.class.getCanonicalName();
        DefaultJsonLayout jsonLayout = new DefaultJsonLayout();
        Logger logger = newLogger(loggerName, null, INFO);

        LogEvent logEvent = buildLogEvent(logger, "test", new Object[0]);
        // {"time":"2024-04-27T17:37:22.166","thread":"main","level":"INFO","loggerName":"com.github.logger.LoggerTest","message":"test"}
        String expected = "{\"eventTime\":\"" + getLogEventTimeStr(logEvent) + "\",\"threadName\":\"main\",\"level\":\"INFO\",\"loggerName\":\"" + loggerName + "\",\"message\":\"test\"}";
        String format = jsonLayout.format(logEvent);
        System.out.printf("expected: %s %nformat: %s %n", expected, format);
        Assertions.assertEquals(
            expected,
            format
        );

    }

    @Test
    public void getLogger() {
        // test systemProperty set
        System.setProperty(Constants.SYSTEM_PROPERTY_LOG_LEVEL, TEST_DEFAULT_LEVEL.name());
        System.setProperty(Constants.SYSTEM_PROPERTY_LOG_DIR, LOG_DIR.toString());

        ReflectUtils.MethodWrapper init = ReflectUtils.getMethodWrapper(LoggerFactory.class.getName(), "init", true);
        init.invoke((Object[]) null);

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

    @Test
    public void setLayout() {
        // test systemProperty set layout impl
        System.setProperty(Constants.SYSTEM_PROPERTY_LOG_LAYOUT_IMPL, DefaultJsonLayout.class.getName());

        ReflectUtils.MethodWrapper init = ReflectUtils.getMethodWrapper(LoggerFactory.class.getName(), "init", true);
        init.invoke((Object[]) null);
        Object appenderCombiner = getFieldValue(LoggerFactory.class, "APPENDER_COMBINER", true);
        boolean jsonLayoutExists = false;
        List<Appender<?>> appenderList = getFieldValue(appenderCombiner, "appenderList", true);
        for (Appender<?> appender : Objects.requireNonNull(appenderList)) {
            if (appender instanceof DefaultPrintStreamAppender && Objects.equals(
                Objects.requireNonNull(getFieldValue(appender, "layout", true)).getClass(),
                DefaultJsonLayout.class
            )) {
                jsonLayoutExists = true;
            }
            Assertions.assertTrue(jsonLayoutExists);
        }
    }

    @Test
    public void loadLayout() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        checkLoadLayout("io.github.supervate.vlog.layout.DefaultJsonLayout", DefaultJsonLayout.class, false);
        checkLoadLayout("io.github.supervate.vlog.layout.DefaultLineLayout", DefaultLineLayout.class, false);
        checkLoadLayout(LoggerTest.class.getCanonicalName(), null, true);
        checkLoadLayout(InvalidEventLayout.class.getCanonicalName(), null, true);
    }

    @SuppressWarnings({ "unchecked" })
    private static void checkLoadLayout(
        String className,
        Class<?> expectedClass,
        boolean testLoadFailed
    ) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method loadLayout = LoggerFactory.class.getDeclaredMethod("loadLayout", String.class);
        loadLayout.setAccessible(true);
        if (testLoadFailed) {
            try {
                loadLayout.invoke(null, InvalidEventLayout.class.getName());
                Assertions.fail("Should throw IllegalArgumentException");
            } catch (Exception e) {
                Assertions.assertNotNull(e.getCause());
                Assertions.assertInstanceOf(IllegalArgumentException.class, e.getCause());
                Assertions.assertTrue(e.getCause().getMessage().contains("Invalid layout class"));
            }
        } else {
            Layout<LogEvent> defaultJsonLayout = (Layout<LogEvent>) loadLayout.invoke(null, className);
            Assertions.assertSame(defaultJsonLayout.getClass(), expectedClass);
        }
    }

    public static class InvalidEventLayout implements Layout<String> {
        @Override
        public String format(String logEvent) {
            throw new UnsupportedOperationException();
        }
    }

}
