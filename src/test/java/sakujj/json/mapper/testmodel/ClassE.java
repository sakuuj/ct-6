package sakujj.json.mapper.testmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassE {
    List<String> stringList;
    List<ClassF> classFList;
}
