package io.github.supervate.vlog.layout;

import io.github.supervate.vlog.Logger;
import io.github.supervate.vlog.common.ThrowableUtils;
import io.github.supervate.vlog.event.LogEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static io.github.supervate.vlog.common.Constants.*;


/**
 * 默认日志格式化器
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultJsonLayout implements Layout<LogEvent> {

    public static final String UN_DEFINE = "unDefine";

    @Override
    public String format(LogEvent event) {
        if (event != null && event.getMessage() != null) {
            Logger logger = event.getLogger();
            StringBuilder sb = new StringBuilder();
            sb.append(LEFT_BIG_BRACKET);

            appendJsonItem(
                "eventTime",
                Optional
                    .ofNullable(event.getEventTime())
                    .map(eventTime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getEventTime()), ZoneId.systemDefault()))
                    .map(LocalDateTime::toString)
                    .orElse(UN_DEFINE),
                sb
            ).append(COMMA);
            appendJsonItem("threadName", Optional.ofNullable(event.getThreadName()).orElse(UN_DEFINE), sb).append(COMMA);
            appendJsonItem("level", Optional.ofNullable(event.getLevel()).map(Enum::name).orElse(UN_DEFINE), sb).append(COMMA);
            appendJsonItem("loggerName", Optional.ofNullable(logger).map(Logger::getName).orElse(UN_DEFINE), sb).append(COMMA);

        appendJsonItem(
            "message",
            formatMessage(event.getMessage(), event.getArguments()),
            sb
        );
        if (event.getThrowable() != null) {
            sb.append(COMMA);
            appendJsonItem("exception", ThrowableUtils.throwableToStr(event.getThrowable()), sb);
        }
        sb.append(RIGHT_BIG_BRACKET);
        return sb.toString();
    }
        return EMPTY_OBJECT_SYMBOL;
}

private static StringBuilder appendJsonItem(String key, String value, StringBuilder sb) {
    sb.append(DOUBLE_QUOTES);
    sb.append(key);
    sb.append(DOUBLE_QUOTES);
    sb.append(COLON);
    sb.append(DOUBLE_QUOTES);
    sb.append(value);
    sb.append(DOUBLE_QUOTES);
    return sb;
}


}
