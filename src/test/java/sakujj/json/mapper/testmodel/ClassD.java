package sakujj.json.mapper.testmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassD {
    private List<Integer> integers;

    private Set<ClassE> classESet;
}
