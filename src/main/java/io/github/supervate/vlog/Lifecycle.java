package io.github.supervate.vlog;

/**
 * 生命周期
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public interface Lifecycle {
    boolean started();
    boolean start();
    boolean stop();
}
