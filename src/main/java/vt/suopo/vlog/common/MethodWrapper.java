package vt.suopo.vlog.common;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Method Wrapper for avoiding NPE.
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public class MethodWrapper {
    private Method method;
    private Object object;

    private MethodWrapper(){}

    public static MethodWrapper of(Object obj, String methodName, boolean declared, Class<?>... parameterTypes) {
        MethodWrapper wrapper = new MethodWrapper();
        if (Objects.isNull(obj)) {
            return wrapper;
        }
        wrapper.object = obj;

        Class<?> clazz = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        try {
            final String key = clazz.getName() + "." + methodName;
            wrapper.method = CachePools.getMethodCache(key);
            if (Objects.isNull(wrapper.method)) {
                wrapper.method = declared
                                 ? clazz.getDeclaredMethod(methodName, parameterTypes)
                                 : clazz.getMethod(methodName, parameterTypes);
                wrapper.method.setAccessible(true);
                CachePools.setMethodCache(key, wrapper.method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Object... args) {
        if (Objects.isNull(object)) {
            return null;
        }

        try {
            return (T)method.invoke(object, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
