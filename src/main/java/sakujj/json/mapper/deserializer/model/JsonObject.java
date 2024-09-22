package sakujj.json.mapper.deserializer.model;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode(callSuper = true)
public final class JsonObject<T> extends JsonEntity<T> {
    private final Map<String, Object> properties = new HashMap<>();

    private Class<T> clazz = super.clazz;

    public JsonObject(Class<T> clazz) {
        super(clazz);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }
}
