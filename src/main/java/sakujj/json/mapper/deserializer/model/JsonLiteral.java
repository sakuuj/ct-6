package sakujj.json.mapper.deserializer.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public final class JsonLiteral<T> extends JsonEntity<T> {
    private final T literal;

    private Class<T> clazz = super.clazz;

    public JsonLiteral(T literal, Class<T> clazz) {
        super(clazz);
        this.literal = literal;
    }
}
