package sakujj.json.mapper;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class ReflectionUtils {

    public static Field getAccessibleFieldWithName(List<Field> classFields, String fieldName, String noFieldExceptionMessage) {
        Optional<Field> optField = classFields.stream()
                .filter(f -> f.getName().equals(fieldName))
                .findAny();
        return optField.orElseThrow(() -> new IllegalArgumentException(noFieldExceptionMessage));
    }

    public static String getDeclaredFieldName(Field field) {
        field.setAccessible(true);
        String name = field.getName();
        field.setAccessible(false);

        return name;
    }

    public static Class<?> getFirstTypeArgumentOfGenericField(Field field) {
        return (Class<?>) (
                ((ParameterizedType) field.getGenericType())
                        .getActualTypeArguments()[0]
        );
    }

    public static boolean isDeclaredFieldStatic(Field f) {
        f.setAccessible(true);
        boolean isStatic = Modifier.isStatic(f.getModifiers());
        f.setAccessible(false);
        return isStatic;
    }

    public static boolean isDeclaredFieldTransient(Field f) {
        f.setAccessible(true);
        boolean isTransient = Modifier.isTransient(f.getModifiers());
        f.setAccessible(false);
        return isTransient;
    }

    public static <T> void setField(Field f, T obj, Object newVal) {
        try {
            f.set(obj, newVal);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getDeclaredFieldValue(Object fieldOwner, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(fieldOwner);
            field.setAccessible(false);
            return value;

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
