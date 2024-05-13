package io.github.supervate.vlog.layout;

import static io.github.supervate.vlog.common.Constants.*;

/**
 * 日志格式化器
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public interface Layout<E> {

    String format(E event);

    default String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        int matchedArgIndex = 0;
        char[] charArray = message.toCharArray();
        StringBuilder messageBuilder = new StringBuilder();
        boolean leftBracketMatched = false;
        for (char c : charArray) {
            if (leftBracketMatched) {
                // clear left flag
                leftBracketMatched = false;
                // try to match argument
                if (c == RIGHT_BIG_BRACKET_CHAR) {
                    // append message
                    messageBuilder.append(matchedArgIndex < args.length ? args[matchedArgIndex++] : EMPTY_OBJECT_SYMBOL);
                    continue;
                } else {
                    messageBuilder.append(LEFT_BIG_BRACKET);
                }
            }
            if (c == LEFT_BIG_BRACKET_CHAR) {
                leftBracketMatched = true;
                continue;
            }
            messageBuilder.append(c);
        }
        return messageBuilder.toString();
    }

}
