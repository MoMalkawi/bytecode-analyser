package org.rsminion.tools.searchers.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class MethodMetaData {

    private String owner;

    private String name;

    private String description;

    public static MethodMetaData create(ClassNode owner, MethodNode method) {
        return new MethodMetaData(owner.name, method.name, method.desc);
    }

    public static MethodMetaData create(MethodInsnNode methodInsnNode) {
        return new MethodMetaData(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

}
