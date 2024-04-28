package vt.suopo.vlog.appender;

import vt.suopo.vlog.Layout;
import vt.suopo.vlog.event.Level;
import vt.suopo.vlog.event.LogEvent;
import vt.suopo.vlog.exception.CreateAppenderException;

import java.io.PrintStream;
import java.util.Objects;

/**
 * 默认日志输出-控制台
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class DefaultPrintStreamAppender extends AsyncAppender<LogEvent> {

    Layout<LogEvent> layout;
    private final PrintStream outPrintStream;
    private final PrintStream errPrintStream;

    public DefaultPrintStreamAppender(
        Layout<LogEvent> layout,
        PrintStream outPrintStream,
        PrintStream errPrintStream
    ) {
        super();
        if (Objects.isNull(outPrintStream)){
            throw new CreateAppenderException("outPrintStream missing.");
        }
        if (Objects.isNull(errPrintStream)){
            throw new CreateAppenderException("errPrintStream missing.");
        }
        this.layout = layout;
        this.outPrintStream = outPrintStream;
        this.errPrintStream = errPrintStream;
    }

    @Override
    public boolean support(LogEvent event) {
        return event != null;
    }

    @Override
    void doAppend(LogEvent event) {
        if (started()) {
            if (Objects.equals(event.getLevel(), Level.ERROR)) {
                errPrintStream.print(layout.format(event));
            } else {
                outPrintStream.print(layout.format(event));
            }
        }
    }

}
