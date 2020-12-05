package malte0811.controlengineering.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectionUtils {
    public static MethodHandle findConstructor(Class<?> object, Class<?>... args) {
        try {
            return MethodHandles.publicLookup()
                    .findConstructor(object, MethodType.methodType(void.class, args));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
