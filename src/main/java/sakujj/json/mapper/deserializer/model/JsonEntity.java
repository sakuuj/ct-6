package sakujj.json.mapper.deserializer.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public sealed class JsonEntity<T> permits JsonList, JsonLiteral, JsonObject {

    protected Class<T> clazz;
    public JsonEntity(Class<T> clazz) {
        this.clazz = clazz;
    }
}