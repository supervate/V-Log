package io.github.supervate.vlog;

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
}
