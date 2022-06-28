package org.rsminion.tools.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.tools.searchers.Searcher;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class SearchUtils {

    public static boolean isOverridden(ClassNode owner, MethodNode method) {
        String superName;
        ClassNode curr = owner;
        while((superName = curr.superName) != null && !superName.contains("java/lang/Object")) {
            if(superName.startsWith("java")) {
                curr = new ClassNode();
                try {
                    ClassReader cr = new ClassReader(superName);
                    cr.accept(curr, 0);
                } catch (Exception ignored) { }
            } else curr = GamePack.get(superName);
            if(Searcher.classContainsMethod(curr, method.name, method.desc))
                return true;
        }
        return false;
    }

    public static String[] getParameters(MethodNode method) {
        //if(method.desc.contains("()")) return Utils.EMPTY_ARRAY;
        List<String> descriptions = new ArrayList<>();
        StringBuilder objectDescBuilder = new StringBuilder();
        StringBuilder descBuilder = new StringBuilder();
        int arrayDimensions = 0;
        char[] desc = method.desc.toCharArray();
        for(char c : desc) {
            if(c == ')') {

                if(arrayDimensions > 0)
                    Logger.warning("Array Identifier Leak: " + arrayDimensions +
                            " [ From Desc: " + method.desc);

                if(objectDescBuilder.length() > 0)
                    Logger.warning("Object Description Leak: " + objectDescBuilder.toString() +
                            " From Desc: " + method.desc);

                break;
            }

            if(objectDescBuilder.length() > 0) {
                objectDescBuilder.append(c);
                if(c == ';') {
                    descriptions.add(objectDescBuilder.toString());
                    objectDescBuilder.delete(0, objectDescBuilder.length());
                }
            } else {
                if(c == '[')
                    arrayDimensions++;
                else if(c == 'L') {
                    for(int i = 0; i < arrayDimensions; i++)
                        objectDescBuilder.append("[");
                    arrayDimensions = 0;
                    objectDescBuilder.append('L');
                } else if(c != '(') {
                    for(int i = 0; i < arrayDimensions; i++)
                        descBuilder.append("[");
                    arrayDimensions = 0;
                    descriptions.add(descBuilder.append(c).toString());
                    descBuilder.delete(0, descBuilder.length());
                }
            }
        }
        return descriptions.toArray(new String[0]);
    }

    public static boolean containsParams(MethodNode method, String... params) {
        String[] parameters = getParameters(method);
        if(parameters.length > 0) {
            for(String param : params) {
                if(Arrays.stream(parameters).noneMatch(s -> s.equals(param)))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static int countParam(MethodNode method, String param) {
        String[] params = getParameters(method);
        int count = 0;
        for(String p : params) {
            if(p.equals(param))
                count++;
        }
        return count;
    }

    public static boolean isStandaloneObject(ClassNode clazz) {
        return clazz.superName.equals("java/lang/Object");
    }

    public static boolean isParent(ClassNode clazz, RSClass supposedParent) {
        return clazz.superName.equals(supposedParent.getObfName());
    }

    public static boolean isParent(ClassNode clazz, String parentName) {
        RSClass parent = Matchers.getClass(parentName);
        return parent != null && isParent(clazz, parent);
    }

    public static boolean hasInterfaces(ClassNode clazz, String... interfacePaths) {
        for(String ip : interfacePaths) {
            if(!clazz.interfaces.contains(ip))
                return false;
        }
        return true;
    }

    public static int countObjectFields(ClassNode clazz) {
        List<FieldNode> fields = clazz.fields;
        int count = 0;
        for(FieldNode field : fields) {
            if(!Modifier.isStatic(field.access))
                count++;
        }
        return count;
    }

    public static int countObjectMethods(ClassNode clazz) {
        List<MethodNode> methods = clazz.methods;
        int count = 0;
        for(MethodNode method : methods) {
            if(!Modifier.isStatic(method.access))
                count++;
        }
        return count;
    }

    public static boolean containsAnyOpcodes(MethodNode method, int... opcodes) {
        AbstractInsnNode[] instructions = method.instructions.toArray();
        for(AbstractInsnNode ain : instructions) {
            if(Arrays.stream(opcodes).anyMatch(o -> ain.getOpcode() == o))
                return true;
        }
        return false;
    }

    public static MethodNode getLeastInstructionLength(MethodNode... methods) {
        MethodNode least = null;
        int length = Integer.MAX_VALUE;
        for(MethodNode method : methods) {
            if(least == null || method.instructions.size() < length) {
                least = method;
                length = method.instructions.size();
            }
        }
        return least;
    }

    public static MethodNode getMostInstructionLength(MethodNode... methods) {
        MethodNode least = null;
        int length = 0;
        for(MethodNode method : methods) {
            if(least == null || method.instructions.size() > length) {
                least = method;
                length = method.instructions.size();
            }
        }
        return least;
    }

    public static boolean hasFieldType(ClassNode clazz, String type) {
        List<FieldNode> fields = clazz.fields;
        for(FieldNode f : fields) {
            if(f.desc.equals(type))
                return true;
        }
        return false;
    }

    public static boolean hasFieldTypes(ClassNode clazz, String... types) {
        List<FieldNode> fields = clazz.fields;
        for(String type : types) {
            boolean found = false;
            for(FieldNode field : fields) {
                if (field.desc.equals(type)) {
                    found = true;
                    break;
                }
            }
            if(!found)
                return false;
        }
        return true;
    }

    public static boolean isVoid(MethodNode method) {
        return method.desc.contains(")V");
    }

    public static boolean isReturnType(MethodNode method, String returnType) {
        return method.desc.endsWith(")"+returnType);
    }

    public static boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>");
    }

    public static boolean isPublicFinal(int access) {
        int PUBLIC_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
        return (access & PUBLIC_FINAL) == PUBLIC_FINAL;
    }

    public static boolean isNativeObject(String desc) {
        return desc.equals("Ljava/lang/Object;");
    }

    public static boolean isClass(String name) {
        return name.startsWith("L") && name.endsWith(";");
    }
}
