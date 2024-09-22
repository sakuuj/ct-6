package sakujj.json.mapper.serializer;

import lombok.experimental.UtilityClass;
import sakujj.json.mapper.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

@UtilityClass
public class Serializer {
    public <T> String serialize(T source) {
        StringBuilder sb = new StringBuilder();

        appendPropertyValue(sb, source);

        return sb.toString();
    }

    private boolean isTypeTerminal(Object typeInstance) {
        return (
                typeInstance == null
                        || typeInstance instanceof Number
                        || typeInstance instanceof Boolean
                        || typeInstance instanceof String
                        || typeInstance instanceof UUID
                        || typeInstance instanceof Class<?>
        );
    }

    private void appendPropertyValue(StringBuilder sb, Object typeInstance) {
        if (typeInstance instanceof Collection<?>) {
            appendCollection(sb, (Collection<?>) typeInstance);
            return;
        }

        if (typeInstance instanceof Map<?, ?>) {
            appendMap(sb, (Map<?, ?>) typeInstance);
            return;
        }

        if (isTypeTerminal(typeInstance)) {
            appendTerminalType(sb, typeInstance);
        } else {
            appendObject(sb, typeInstance);
        }
    }

    private static void appendObject(StringBuilder sb, Object obj) {
        sb.append("{");

        List<Field> fields = Arrays
                .stream(obj.getClass().getDeclaredFields())
                .filter(f -> !ReflectionUtils.isDeclaredFieldStatic(f))
                .filter(f -> !ReflectionUtils.isDeclaredFieldTransient(f))
                .toList();

        Iterator<Field> iterator = fields.iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            appendPropertyHavingOwner(
                    sb,
                    obj,
                    field
            );

            if (iterator.hasNext()) {
                sb.append(",");
            }
        }

        sb.append("}");
    }

    private void appendPropertyHavingOwner(StringBuilder sb, Object propertyOwner, Field correspondingField) {
        Object propertyValue = ReflectionUtils.getDeclaredFieldValue(propertyOwner, correspondingField);

        appendInQuotes(sb, ReflectionUtils.getDeclaredFieldName(correspondingField));
        sb.append(":");
        appendPropertyValue(sb, propertyValue);
    }

    private void appendCollection(StringBuilder sb, Collection<?> collection) {
        sb.append("[");

        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object elem = iterator.next();
            appendPropertyValue(sb, elem);

            if (iterator.hasNext()) {
                sb.append(",");
            }
        }

        sb.append("]");
    }

    private void appendTerminalType(StringBuilder sb, Object terminalTypeInstance) {
        if (terminalTypeInstance == null
                || terminalTypeInstance instanceof Number
                || terminalTypeInstance instanceof Boolean) {
            sb.append(terminalTypeInstance);
            return;
        }

        appendInQuotes(sb, terminalTypeInstance.toString());
    }

    private void appendMap(StringBuilder sb, Map<?, ?> map) {
        sb.append("{");

        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            appendEntryTypeProperty(sb, entry);

            if (iterator.hasNext()) {
                sb.append(",");
            }
        }

        sb.append("}");
    }

    private void appendEntryTypeProperty(StringBuilder sb, Map.Entry<?, ?> entry) {
        Object key = entry.getKey();
        Object value = entry.getValue();

        appendInQuotes(sb, key.toString());
        sb.append(":");
        appendPropertyValue(sb, value);
    }

    private void appendInQuotes(StringBuilder sb, String keyValue) {
        sb.append("\"");
        sb.append(keyValue);
        sb.append("\"");
    }
}
