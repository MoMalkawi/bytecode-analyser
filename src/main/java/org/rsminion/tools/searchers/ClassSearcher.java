package org.rsminion.tools.searchers;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.tools.utils.Filter;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@AllArgsConstructor
public class ClassSearcher {

    @Setter
    private ClassNode clazz;

    /**
     * Searched all MethodNodes in clazz for MethodNodes that match condition.
     * @param condition MethodNodes conditions
     * @return Collection of Verified MethodNodes
     */
    public List<MethodNode> findMethods(Filter<MethodNode> condition) {
        List<MethodNode> methods = (List<MethodNode>) clazz.methods;
        return methods != null ? methods.stream().filter(m -> m != null &&
                condition.verify(m)).collect(Collectors.toList()) : null;
    }

    /**
     * Searched all MethodNodes in clazz for the first occurrence of a MethodNode that matches condition.
     * @param condition MethodNode conditions
     * @return MethodNode with condition verified.
     */
    public MethodNode findMethod(Filter<MethodNode> condition) {
        List<MethodNode> methods = (List<MethodNode>) clazz.methods;
        return methods != null ? methods.stream().filter(m -> m != null &&
                condition.verify(m)).findFirst().orElse(null) : null;
    }

    /**
     * Searched all FieldNodes in clazz for FieldNodes that match condition.
     * @param condition FieldNodes conditions
     * @return Collection of Verified FieldNodes
     */
    public FieldNode[] findFields(Filter<FieldNode> condition) {
        List<FieldNode> fields = (List<FieldNode>) clazz.fields;
        return fields != null ? fields.stream().filter(f -> f != null &&
                condition.verify(f)).toArray(FieldNode[]::new) : null;
    }

    /**
     * Searched all FieldNodes in clazz for the first occurrence of a FieldNodes that matches condition.
     * @param condition FieldNodes conditions
     * @return FieldNodes with condition verified.
     */
    public FieldNode findField(Filter<FieldNode> condition) {
        List<FieldNode> fields = (List<FieldNode>) clazz.fields;
        return fields != null ? fields.stream().filter(f -> f != null &&
                condition.verify(f)).findFirst().orElse(null) : null;
    }

    public FieldNode[] findFields(Filter<FieldNode>... conditions) {
        FieldNode[] result = new FieldNode[conditions.length];
        List<FieldNode> fields = (List<FieldNode>) clazz.fields;
        for(FieldNode field : fields) {
            for(int i = 0; i < conditions.length; i++) {
                if(result[i] == null && conditions[i].verify(field)) result[i] = field;
            }
        }
        return result;
    }

    public int[] countFieldNodes(Filter<FieldNode>... fieldConditions) {
        int[] counts = new int[fieldConditions.length];
        List<FieldNode> fields = (List<FieldNode>) clazz.fields;
        for(FieldNode field : fields) {
            for(int i = 0; i < fieldConditions.length; i++) {
                if(fieldConditions[i].verify(field))
                    counts[i]++;
            }
        }
        return counts;
    }

    /**
     * Uses findMethod(Condition<MethodNode> condition) to find MethodNode with methodName
     * @param methodName MethodNode name
     * @return MethodNode with methodName verified.
     */
    public MethodNode findMethodByName(String methodName) {
        return findMethod(m -> m!=null && m.name.equalsIgnoreCase(methodName));
    }


}
