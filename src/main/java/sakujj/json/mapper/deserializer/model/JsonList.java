package sakujj.json.mapper.deserializer.model;

import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString(callSuper = true)
public final class JsonList<T extends JsonEntity<K>, K> extends JsonEntity<K> {

    private Class<K> clazz = super.clazz;
    private List<T> list = new ArrayList<>();

    public JsonList(Class<K> typeArgument) {
        super(typeArgument);
    }

    public void add(T entity) {
        list.add(entity);
    }

    public void addAll(Collection<T> entity) {
        list.addAll(entity);
    }

    public List<T> get() {
        return list;
    }


}
