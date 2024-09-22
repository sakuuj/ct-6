package sakujj.json.mapper.deserializer;

import sakujj.json.mapper.ReflectionUtils;
import sakujj.json.mapper.deserializer.model.JsonEntity;
import sakujj.json.mapper.deserializer.model.JsonList;
import sakujj.json.mapper.deserializer.model.JsonLiteral;
import sakujj.json.mapper.deserializer.model.JsonObject;

import java.lang.reflect.Field;
import java.util.*;

public class Deserializer {
    public static <T> T deserialize(String json, Class<T> clazz) {
        JsonEntity<T> jsonEntity = Parser.parseJson(json, clazz);

        return deserializeObjOrLiteral(jsonEntity);
    }

    public static <T> Collection<T> deserializeCollection(String json, Class<T> clazz, Class<?> collectionClass) {
        JsonList<? extends JsonEntity<T>, T> jsonList = (JsonList<? extends JsonEntity<T>, T>) Parser.parseJson(json, clazz);

        return deserializeList(jsonList, collectionClass);
    }

    private static <T> Collection<T> deserializeList(JsonList<? extends JsonEntity<T>, T> jsonList, Class<?> collectionClass) {
        Collection<T> collection;

        if (jsonList == null) {
            return null;
        }

        if (Set.class.isAssignableFrom(collectionClass)) {
            collection = new HashSet<>();
        } else if (List.class.isAssignableFrom(collectionClass)) {
            collection = new ArrayList<>();
        } else {
            throw new UnsupportedOperationException("No support for "
                    + collectionClass.getName()
                    + " is provided.");
        }

        jsonList.get().forEach(item -> collection.add(deserializeObjOrLiteral(item)));
        return collection;
    }

    private static <T> T deserializeObjOrLiteral(JsonEntity<T> jsonEntity) {
        if (jsonEntity == null) {
            return null;
        }

        if (jsonEntity instanceof JsonLiteral<T> jsonLiteral) {
            return jsonLiteral.getLiteral();
        }

        if (jsonEntity instanceof JsonObject<T> jsonObject) {
            try {
                Class<T> clazz = jsonObject.getClazz();
                T obj = clazz.getDeclaredConstructor().newInstance();

                List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
                fields.forEach(f -> f.setAccessible(true));
                fields.forEach(f -> {
                    JsonEntity<?> fieldJsonEntity = (JsonEntity<?>) jsonObject.getProperty(f.getName());
                    if (fieldJsonEntity instanceof JsonList<?, ?> fieldJsonList) {
                        ReflectionUtils.setField(f, obj, deserializeList(fieldJsonList, f.getType()));
                        return;
                    }

                    ReflectionUtils.setField(f, obj, deserializeObjOrLiteral(fieldJsonEntity));
                });
                fields.forEach(f -> f.setAccessible(false));

                return obj;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        throw new IllegalArgumentException("SHOULD BE OBJECT OR LITERAL");
    }
}
