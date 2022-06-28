package org.rsminion.core.matching.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.classes.RSClass;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RSMethod {

    private String name;

    private String obfName;

    private String owner;

    private String obfOwner;

    private String desc;

    private int access;

    private MethodNode method;

    public static RSMethod create(String name, String owner, String obfOwner, MethodNode method) {
        RSMethod rsm = new RSMethod(name, method.name, owner, obfOwner,
                method.desc, method.access, method);
        return rsm;
    }

    public static RSMethod create(String name, RSClass clazz, MethodNode method) {
        return create(name, clazz.getName(), clazz.getObfName(), method);
    }

}
