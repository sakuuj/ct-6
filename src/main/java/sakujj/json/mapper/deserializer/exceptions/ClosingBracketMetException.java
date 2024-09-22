package sakujj.json.mapper.deserializer.exceptions;

import lombok.Getter;

@Getter
public class ClosingBracketMetException extends RuntimeException{
    private int closingBracketIndex;
    public ClosingBracketMetException(int closingBracketIndex) {
        this.closingBracketIndex = closingBracketIndex;
    }
}
