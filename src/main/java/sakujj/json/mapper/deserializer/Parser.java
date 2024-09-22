package sakujj.json.mapper.deserializer;

import sakujj.json.mapper.ReflectionUtils;
import sakujj.json.mapper.deserializer.exceptions.ClosingBracketMetException;
import sakujj.json.mapper.deserializer.exceptions.NullMetException;
import sakujj.json.mapper.deserializer.model.JsonEntity;
import sakujj.json.mapper.deserializer.model.JsonList;
import sakujj.json.mapper.deserializer.model.JsonLiteral;
import sakujj.json.mapper.deserializer.model.JsonObject;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class Parser {

    private static final String INVALID_JSON_MESSAGE = "Invalid json";
    private static final Map<Class<?>, Function<String, ?>> parseFunctions = new HashMap<>(
            Map.of(
                    String.class, s -> s,
                    Integer.class, Integer::valueOf,
                    int.class, Integer::parseInt,
                    Double.class, Double::valueOf,
                    double.class, Double::parseDouble,
                    UUID.class, UUID::fromString,
                    LocalDate.class, LocalDate::parse
            )
    );

    public static <T> JsonEntity<T> parseJson(String json, Class<T> clazz) {
        String strippedJson = json.strip();

        JsonEntity<T> jsonEntity = null;
        char first = strippedJson.charAt(0);
        switch (first) {
            case '{' -> jsonEntity = parseObject(strippedJson, 1, clazz).getKey();
            case '[' -> jsonEntity = parseList(strippedJson, 1, clazz).getKey();
            default -> {
                try {
                    jsonEntity = parseTerminalClassValue(strippedJson, 0, strippedJson.length(), clazz).getKey();
                } catch (NullMetException ignored) {
                }
            }
        }

        return jsonEntity;
    }

    private static <T> Entry<JsonList<JsonEntity<T>, T>, Integer> parseList(String json, int i, Class<T> typeArgument) {
        JsonList<JsonEntity<T>, T> jsonList = new JsonList<>(typeArgument);
        int startIndex = i;

        while (i < json.length()) {
            try {
                JsonEntity<T> item;
                Integer indexAfterItem;
                try {
                    Entry<? extends JsonEntity<T>, Integer> parsedItem = parseJsonValue(json, i, typeArgument);
                    item = parsedItem.getKey();
                    indexAfterItem = parsedItem.getValue();
                } catch (NullMetException e) {
                    item = null;
                    indexAfterItem = e.getAfterNullIndex();
                }

                jsonList.add(item);
                int endIndex = getEndIndexForListItem(json, indexAfterItem);

                if (json.charAt(endIndex) == ']') {
                    return Map.entry(jsonList, endIndex);
                }

                i = endIndex + 1;
            } catch (ClosingBracketMetException e) {
                if (i == startIndex) {
                    return Map.entry(jsonList, e.getClosingBracketIndex());
                }

                throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
            }
        }

        throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
    }

    private static int getEndIndexForListItem(String json, int fromIndex) {
        int commaIndex = json.indexOf(',', fromIndex);
        int curlyBracketIndex = json.indexOf(']', fromIndex);
        int endIndex = Math.min(commaIndex, curlyBracketIndex);

        if (commaIndex == -1) {
            endIndex = curlyBracketIndex;
        }

        if (endIndex == -1) {
            throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
        }
        return endIndex;
    }

    private static <T> Entry<JsonObject<T>, Integer> parseObject(String json, int i, Class<T> clazz) {
        JsonObject<T> jsonObject = new JsonObject<>(clazz);
        List<Field> classFields = Arrays.asList(clazz.getDeclaredFields());
        classFields.forEach(f -> f.setAccessible(true));

        while (i < json.length()) {
            Entry<String, Integer> parsedKey = parseFirstJsonKey(json, i);
            String propertyKey = parsedKey.getKey();

            i = parsedKey.getValue();
            while (json.charAt(i) != ':') {
                i++;
            }
            i++;

            Field field = ReflectionUtils.getAccessibleFieldWithName(
                    classFields,
                    propertyKey,
                    formNoSuchFieldMessage(propertyKey, clazz.getName()));

            Class<?> fieldTypeArgumentOrClass = getFieldTypeArgumentOrClass(field);

            Object propertyValue;
            int endIndex;
            try {
                Entry<? extends JsonEntity<?>, Integer> parsedValue = parseJsonValue(json, i, fieldTypeArgumentOrClass);
                propertyValue = parsedValue.getKey();
                endIndex = parsedValue.getValue();
            } catch (NullMetException e) {
                propertyValue = null;
                endIndex = e.getAfterNullIndex();
            }

            jsonObject.setProperty(propertyKey, propertyValue);

            endIndex = getEndIndexForPropertyValue(json, endIndex);

            if (json.charAt(endIndex) == '}') {
                classFields.forEach(f -> f.setAccessible(false));
                return Map.entry(jsonObject, endIndex + 1);
            }

            i = endIndex + 1;
        }

        throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
    }

    private static Class<?> getFieldTypeArgumentOrClass(Field field) {
        Class<?> fieldClass = field.getType();
        if (Collection.class.isAssignableFrom(fieldClass)) {
            fieldClass = ReflectionUtils.getFirstTypeArgumentOfGenericField(field);
        }
        return fieldClass;
    }

    private static String formNoSuchFieldMessage(String fieldName, String encolsingClassName) {
        return INVALID_JSON_MESSAGE
                + ": no such field "
                + fieldName
                + " in class "
                + encolsingClassName;
    }

    private static int getEndIndexForPropertyValue(String json, int fromIndex) {
        int commaIndex = json.indexOf(',', fromIndex);
        int curlyBracketIndex = json.indexOf('}', fromIndex);

        int endIndex = Math.min(commaIndex, curlyBracketIndex);

        if (commaIndex == -1) {
            endIndex = curlyBracketIndex;
        }

        if (endIndex == -1) {
            throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
        }
        return endIndex;
    }

    public static <T> Entry<? extends JsonEntity<T>, Integer> parseJsonValue(String json, int fromIndex, Class<T> clazz) {
        fromIndex = skipCharactersUntilValue(json, fromIndex);
        if (json.regionMatches(fromIndex, "null", 0, 4)) {
            throw new NullMetException(fromIndex + 4);
        }

        if (json.charAt(fromIndex) == ']') {
            throw new ClosingBracketMetException(fromIndex);
        }


        if (json.charAt(fromIndex) == '[') {
            return parseList(json, fromIndex + 1, clazz);
        }

        if (isClassTerminal(clazz)) {
            int endIndex = getEndIndexExclusiveForTerminalClassValue(json, fromIndex);
            return parseTerminalClassValue(json, fromIndex, endIndex, clazz);
        }

        if (json.charAt(fromIndex) == '{') {
            return parseObject(json, fromIndex + 1, clazz);
        }

        throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
    }

    private static int skipCharactersUntilValue(String json, int fromIndex) {
        List<Character> charsToSkip = List.of(
                ' ',
                '\n',
                '\r',
                '\t'
        );
        while (fromIndex < json.length() && (charsToSkip.contains(json.charAt(fromIndex)))) {
            fromIndex++;
        }
        if (fromIndex == json.length()) {
            throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
        }
        return fromIndex;
    }

    private static <T> Entry<JsonLiteral<T>, Integer> parseTerminalClassValue(String json, int fromIndex, int endIndex, Class<T> clazz) {
        String value;
        if (json.charAt(fromIndex) == '"') {
            if (json.charAt(endIndex - 1) != '"') {
                throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
            }

            value = json.substring(fromIndex + 1, endIndex - 1);
        } else {
            value = json.substring(fromIndex, endIndex);
        }

        if (value.equals("null")) {
            throw new NullMetException(endIndex);
        }

        var parseFunc = parseFunctions.get(clazz);
        JsonLiteral<T> jsonLiteral = new JsonLiteral<>(
                (T) parseFunc.apply(value),
                clazz
        );

        return Map.entry(jsonLiteral, endIndex);
    }

    public static int getEndIndexExclusiveForTerminalClassValue(String json, int fromIndex) {

        List<Character> charsToMeet;
        if (json.charAt(fromIndex) == '"') {
            charsToMeet = List.of('"');
            fromIndex += 1;
        } else {
            charsToMeet = List.of(
                    ' ',
                    ',',
                    '}',
                    ']',
                    '\n',
                    '\r',
                    '\t'
            );
        }


        for (int i = fromIndex; i < json.length(); i++) {
            if (charsToMeet.contains(json.charAt(i))) {
                if (json.charAt(i) == '"') {
                    return i + 1;
                }

                return i;
            }
        }

        throw new IllegalArgumentException(INVALID_JSON_MESSAGE);
    }

    private static <T> boolean isClassTerminal(Class<T> clazz) {
        Objects.requireNonNull(clazz);

        if (clazz.isPrimitive()) {
            return true;
        }

        return parseFunctions.keySet().stream()
                .anyMatch(terminalClass -> terminalClass.isAssignableFrom(clazz));

    }

    public static Entry<String, Integer> parseFirstJsonKey(String json, int fromIndex) {
        while (json.charAt(fromIndex) != '"') {
            fromIndex++;
        }
        int keyEndIndex = json.indexOf('"', fromIndex + 1);
        String key = json.substring(fromIndex + 1, keyEndIndex);

        return Map.entry(key, keyEndIndex + 1);
    }


}
