package org.rsminion.tools.searchers;

import org.objectweb.asm.tree.*;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.Filter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class Searcher {

    /* --------------- Combined --------------- */

    public static Pattern deepLinearSearch(Filter<MethodNode> methodCondition,
                                           Filter<Pattern> patternCondition,
                                           int patternInstance, int... pattern) {
        MethodSearcher searcher = new MethodSearcher(null);
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            if(methods != null && methods.size() > 0) {
                for(MethodNode method : methods) {
                    if(methodCondition == null || methodCondition.verify(method)) {
                        searcher.setMethod(method);
                        Pattern result = searcher.
                                linearSearch(patternCondition, 0, method.instructions.size(),
                                        patternInstance, pattern);
                        if(result.isFound())
                            return result;
                    }
                }
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public static Pattern deepLinearSearch(Filter<Pattern> patternCondition,
                                           int patternInstance, int... pattern) {
        return deepLinearSearch(null, patternCondition, patternInstance, pattern);
    }

    public static Pattern deepLinearSearch(int patternInstance, int... pattern) {
        return deepLinearSearch(null, null, patternInstance, pattern);
    }

    public static Pattern deepSingularSearch(Filter<MethodNode> methodCondition,
                                             Filter<AbstractInsnNode> patternCondition,
                                             int patternInstance, int pattern) {
        MethodSearcher searcher = new MethodSearcher(null);
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            if(methods != null && methods.size() > 0) {
                for(MethodNode method : methods) {
                    if(methodCondition == null || methodCondition.verify(method)) {
                        searcher.setMethod(method);
                        Pattern result = searcher.
                                singularSearch(patternCondition, patternInstance, pattern);
                        if(result.isFound())
                            return result;
                    }
                }
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public static Pattern deepSingleSearch(Filter<AbstractInsnNode> patternCondition,
                                             int patternInstance, int pattern) {
        return deepSingularSearch(null, patternCondition, patternInstance, pattern);
    }

    public static Pattern deepSingleSearch(int patternInstance, int pattern) {
        return deepSingularSearch(null, null, patternInstance, pattern);
    }

    public static Pattern deepJumpSearch(Filter<MethodNode> methodCondition,
                                         Filter<Pattern> patternCondition,
                                         int jumpOpcode, int maxNumberOfLines,
                                         int patternInstance, int... pattern) {
        MethodSearcher searcher = new MethodSearcher(null);
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            if(methods != null && methods.size() > 0) {
                for(MethodNode method : methods) {
                    if(methodCondition == null || methodCondition.verify(method)) {
                        searcher.setMethod(method);
                        Pattern result = searcher.
                                jumpSearch(patternCondition, jumpOpcode, 0, maxNumberOfLines,
                                        patternInstance, pattern);
                        if(result.isFound())
                            return result;
                    }
                }
            }
        }
        return Pattern.EMPTY_PATTERN;
    }

    public static Pattern deepJumpSearch(Filter<Pattern> patternCondition,
                                         int jumpOpcode, int maxNumberOfLines,
                                         int patternInstance, int... pattern) {
        return deepJumpSearch(null, patternCondition, jumpOpcode, maxNumberOfLines,
                patternInstance, pattern);
    }

    public static Pattern deepJumpSearch(int jumpOpcode, int maxNumberOfLines,
                                         int patternInstance, int... pattern) {
        return deepJumpSearch(null, null, jumpOpcode, maxNumberOfLines,
                patternInstance, pattern);
    }

    /* --------------- Patterns --------------- */

    public static Pattern patternSearch(MethodNode method, Filter<Pattern> patternCondition,
                                              int startLine, int endLine,
                                              int patternInstance, int... pattern) {
        return new MethodSearcher(method).linearSearch(patternCondition, startLine, endLine,
                patternInstance, pattern);
    }

    public static Pattern patternSearch(MethodNode method, int patternInstance, int... pattern) {
        return new MethodSearcher(method).linearSearch(null, patternInstance, 0,
                method.instructions.size(), pattern);
    }

    public static Pattern singularSearch(MethodNode method,
                                         Filter<AbstractInsnNode> patternCondition,
                                                int startLine, int endLine,
                                                int patternInstance, int pattern) {
        return new MethodSearcher(method).singularSearch(patternCondition, startLine, endLine,
                patternInstance, pattern);
    }

    public static Pattern singularSearch(MethodNode method,
                                         Filter<AbstractInsnNode> patternCondition,
                                                int patternInstance, int pattern) {
        return new MethodSearcher(method).singularSearch(patternCondition, patternInstance, pattern);
    }

    public static Pattern singularSearch(MethodNode method,
                                                int startLine, int endLine,
                                                int patternInstance, int pattern) {
        return new MethodSearcher(method).singularSearch(null, startLine, endLine,
                patternInstance, pattern);
    }

    public static Pattern singularSearch(MethodNode method,
                                                int patternInstance, int pattern) {
        return new MethodSearcher(method).singularSearch(patternInstance, pattern);
    }

    public static Pattern jumpSearch(MethodNode method, Filter<Pattern> patternCondition,
                                            int jumpOpcode, int startLine, int maxNumberOfLines,
                                            int patternInstance, int... pattern) {
        return new MethodSearcher(method).jumpSearch(patternCondition, jumpOpcode, startLine,
                maxNumberOfLines, patternInstance, pattern);
    }

    public static Pattern jumpSearch(MethodNode method, int jumpOpcode, int maxNumberOfLines,
                                            int patternInstance, int... pattern) {
        return new MethodSearcher(method).jumpSearch(jumpOpcode,
                maxNumberOfLines, patternInstance, pattern);
    }

    public static Pattern singleJumpSearch(MethodNode method, Filter<AbstractInsnNode> patternCondition,
                                            int jumpOpcode, int startLine, int maxNumberOfLines,
                                            int patternInstance, int pattern) {
        return new MethodSearcher(method).singularJumpSearch(patternCondition, jumpOpcode, startLine,
                maxNumberOfLines, patternInstance, pattern);
    }

    public static Pattern singleJumpSearch(MethodNode method, int jumpOpcode, int maxNumberOfLines,
                                            int patternInstance, int pattern) {
        return new MethodSearcher(method).singularJumpSearch(jumpOpcode,
                maxNumberOfLines, patternInstance, pattern);
    }

    /* --------------- Methods --------------- */

    /**
     * Searches all ClassNodes for the first occurrence of a MethodNode that matches condition.
     * @param condition MethodNode conditions
     * @return MethodNode with condition verified.
     */
    public static MethodNode deepFindMethod(Filter<MethodNode> condition) {
        ClassSearcher searcher = new ClassSearcher(null);
        MethodNode method;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(clazz != null) {
                searcher.setClazz(clazz);
                if((method = searcher.findMethod(condition)) != null)
                    return method;
            }
        }
        return null;
    }

    public static List<MethodNode> deepFindMethods(Filter<MethodNode> condition) {
        List<MethodNode> result = new ArrayList<>();
        ClassSearcher searcher = new ClassSearcher(null);
        MethodNode method;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(clazz != null) {
                searcher.setClazz(clazz);
                if((method = searcher.findMethod(condition)) != null)
                    result.add(method);
            }
        }
        return result;
    }

    /**
     * Creates an instance of ClassSearcher and uses findMethod(Condition<MethodNode> condition)
     * @param condition MethodNode conditions.
     * @param clazz ClassNode to search in
     * @return MethodNode with condition verified.
     */
    public static MethodNode findMethod(Filter<MethodNode> condition, ClassNode clazz) {
        return new ClassSearcher(clazz).findMethod(condition);
    }

    /**
     * Searches all ClassNodes for the first occurrence of a FieldNode that matches condition.
     * @param condition FieldNode conditions
     * @return FieldNode with condition verified.
     */
    public static FieldNode deepFindField(Filter<FieldNode> condition) {
        ClassSearcher searcher = new ClassSearcher(null);
        FieldNode field;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(clazz != null) {
                searcher.setClazz(clazz);
                if((field = searcher.findField(condition)) != null)
                    return field;
            }
        }
        return null;
    }

    /**
     * Creates an instance of ClassSearcher and uses findField(Condition<FieldNode> condition)
     * @param condition FieldNode conditions.
     * @param clazz ClassNode to search in
     * @return FieldNode with condition verified.
     */
    public static FieldNode findField(Filter<FieldNode> condition, ClassNode clazz) {
        return new ClassSearcher(clazz).findField(condition);
    }

    public static void findLocations(String owner, String name) {
        MethodSearcher methodSearcher = new MethodSearcher();
        Pattern[] result;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                methodSearcher.setMethod(method);
                result = methodSearcher.searchForAllKnown(owner, name);
                if(result.length > 0)
                    System.out.println("Class: " + clazz.name + " Method: " +
                            method.name + " Count: " + result.length);
            }
        }
    }

    public static int[] countFieldNodes(ClassNode clazz, Filter<FieldNode>... conditions) {
        return new ClassSearcher(clazz).countFieldNodes(conditions);
    }

    public static boolean classContainsMethod(ClassNode clazz, String methodName, String methodDesc) {
        return findMethod(m -> m.name.equals(methodName) &&
                m.desc.equals(methodDesc), clazz) != null;
    }

    public static boolean classContainsFieldDesc(ClassNode clazz, String desc) {
        List<FieldNode> fields = clazz.fields;
        for(FieldNode field : fields) {
            if(!Modifier.isStatic(field.access) && field.desc.equals(desc))
                return true;
        }
        return false;
    }

    public static boolean classContainsMethodReturnType(ClassNode clazz, String returnType) {
        List<MethodNode> methods = clazz.methods;
        for(MethodNode method : methods) {
            if(!Modifier.isStatic(method.access) && method.desc.endsWith(returnType))
                return true;
        }
        return false;
    }
}
