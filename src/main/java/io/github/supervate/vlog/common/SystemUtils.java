package io.github.supervate.vlog.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * 系统工具
 *
 * @author supervate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class SystemUtils {

    private SystemUtils() {}

    /**
     * @throws java.nio.file.InvalidPathException if a {@code Path} object cannot be constructed from the abstract
     *                                            path (see {@link java.nio.file.FileSystem#getPath FileSystem.getPath})
     */
    public static Optional<Path> getSysPropertyPath(String name) {
        return Optional.ofNullable(System.getProperty(name)).map(path -> new File(System.getProperty(name)).toPath());
    }

    public static Optional<Boolean> getSysPropertyBool(String name) {
        return Optional.ofNullable(System.getProperty(name)).map(Boolean::parseBoolean);
    }

    public static Optional<Integer> getSysPropertyInteger(String name) {
        return Optional.ofNullable(System.getProperty(name)).map(Integer::parseInt);
    }

    public static Optional<String> getSysProperty(String name) {
        return Optional.ofNullable(System.getProperty(name));
    }

    public static Path getSysTempDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

}
