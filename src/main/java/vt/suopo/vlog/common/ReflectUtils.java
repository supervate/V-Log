package vt.suopo.vlog.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Reflect Utils
 *
 * @author suopovate
 * @since 2024/04/27
 * <p>
 * All rights Reserved.
 */
public final class ReflectUtils {

    private ReflectUtils() {}

    /**
     * get public member from class or super class
     */
    public static MethodWrapper getMethodWrapper(String className, String methodName, Class<?>... parameterTypes) {
        return getMethodWrapper(className, methodName, false, parameterTypes);
    }

    /**
     * get public member from class or super class or get declared member form class
     */
    public static MethodWrapper getMethodWrapper(
        String className, String methodName, boolean declared, Class<?>... parameterTypes
    ) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            System.err.println(ThrowableUtils.throwableToStr(e));
        }
        return MethodWrapper.of(clazz, methodName, declared, parameterTypes);
    }

    /**
     * get public member from object's class or super class
     */
    public static MethodWrapper getMethodWrapper(Object obj, String methodName, Class<?>... parameterTypes) {
        return getMethodWrapper(obj, methodName, false, parameterTypes);
    }

    /**
     * get public member from object's class or super class or get declared field form class
     */
    public static MethodWrapper getMethodWrapper(Object obj, String methodName, boolean declared, Class<?>... parameterTypes) {
        return MethodWrapper.of(obj, methodName, declared, parameterTypes);
    }

    /**
     * get public field from object's class or super class
     */
    public static Field getField(Object obj, String fieldName) {
        return getField(obj, fieldName, false);
    }

    /**
     * get public field from object's class or super class or get declared field form class
     */
    public static Field getField(Object obj, String fieldName, boolean declared) {
        if (Objects.isNull(obj)) {
            return null;
        }

        Class<?> clazz = obj instanceof Class<?> ? (Class<?>) obj : obj.getClass();
        String cacheKey = clazz.getName() + "." + fieldName;
        Field field = CachePools.getFieldCache(cacheKey);
        if (Objects.isNull(field)) {
            try {
                field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
                field.setAccessible(true);
                CachePools.setFieldCache(cacheKey, field);
            } catch (Exception e) {
                System.err.println(ThrowableUtils.throwableToStr(e));
                return null;
            }
        }
        return field;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Field field = getField(obj, fieldName, false);

        try {
            return Objects.isNull(field) ? null : (T) field.get(obj);
        } catch (Exception e) {
            System.err.println(ThrowableUtils.throwableToStr(e));
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName, boolean declared) {
        Field field = getField(obj, fieldName, declared);

        try {
            return Objects.isNull(field) ? null : (T) field.get(obj);
        } catch (Exception e) {
            System.err.println(ThrowableUtils.throwableToStr(e));
            return null;
        }
    }

    public static void setFieldValue(String className, String fieldName, Object value) {
        setFieldValue(className, fieldName, value, false);
    }

    public static void setFieldValue(String className, String fieldName, Object value, boolean declared) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(ThrowableUtils.throwableToStr(e));
            return;
        }
        setFieldValue(clazz, fieldName, value, declared);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        setFieldValue(obj, fieldName, value, false);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value, boolean declared) {
        Field field = getField(obj, fieldName, declared);
        if (Objects.isNull(field)) {
            return;
        }
        try {
            if (obj instanceof Class) {
                field.set(null, value);
            } else {
                field.set(obj, value);
            }
        } catch (Exception e) {
            System.err.println(ThrowableUtils.throwableToStr(e));
        }
    }

    public static final class CachePools {
        private CachePools() {}

        private static final Map<String, Field> FIELD_POOL_CACHE = new ConcurrentHashMap<>();
        private static final Map<String, Method> METHOD_POOL_CACHE = new ConcurrentHashMap<>();

        public static Field getFieldCache(String key) {
            return FIELD_POOL_CACHE.get(key);
        }

        public static void setFieldCache(String key, Field field) {
            FIELD_POOL_CACHE.put(key, field);
        }

        public static Method getMethodCache(String key) {
            return METHOD_POOL_CACHE.get(key);
        }

        public static void setMethodCache(String key, Method method) {
            METHOD_POOL_CACHE.put(key, method);
        }
    }

    public static class MethodWrapper {
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
                System.err.println(ThrowableUtils.throwableToStr(e));
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
                System.err.println(ThrowableUtils.throwableToStr(e));
                return null;
            }
        }
    }

}
