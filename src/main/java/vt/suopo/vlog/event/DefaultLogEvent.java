package vt.suopo.vlog.event;


import vt.suopo.vlog.Logger;

/**
 * 默认日志事件
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultLogEvent extends AbstractLogEvent {

    public DefaultLogEvent(Level level, String threadName, Long eventTime, Logger logger, String message, Object[] arguments) {
        super(level, threadName, eventTime, logger, message, arguments);
    }

    public DefaultLogEvent(
        Level level,
        String threadName,
        Long eventTime,
        Logger logger,
        String message,
        Object[] arguments,
        Throwable throwable
    ) {
        super(level, threadName, eventTime, logger, message, arguments, throwable);
    }

}
