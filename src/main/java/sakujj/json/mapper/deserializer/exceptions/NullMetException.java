package sakujj.json.mapper.deserializer.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NullMetException extends RuntimeException{
    int AfterNullIndex;
}
