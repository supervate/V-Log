package vt.suopo.vlog.common;

/**
 * 日志常量
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class Constants {

    public static final String SPACE = " ";
    public static final String LEFT_MIDDLE_BRACKET = "[";
    public static final String RIGHT_MIDDLE_BRACKET = "]";
    public static final String CAUSED_BY = "Caused by: ";

    public static final String SYSTEM_PROPERTY_LOG_DIR = "vt.vlog.dir";
    public static final String SYSTEM_PROPERTY_LOG_LEVEL = "vt.vlog.level";
    public static final String SYSTEM_PROPERTY_LOG_FILE_RETENTION = "vt.vlog.file.retention";
    public static final String SYSTEM_PROPERTY_LOG_FILE_SIZE = "vt.vlog.file.size";

    public static final int DEFAULT_LOG_FILE_RETENTION = 7;
    public static final int DEFAULT_LOG_FILE_SIZE = 0;

}
