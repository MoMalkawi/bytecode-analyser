package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.data.MethodMetaData;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Parameters extends Deobfuscator {

    private final Set<MethodMetaData> manipulatedMethods = new HashSet<>();


    private void fixDescription(MethodNode method) {
        StringBuilder descBuilder = new StringBuilder(method.desc);
        int endIndex = descBuilder.indexOf(")");
        /* Fix Swapped Array Identifier */
        int arrayIdentifier = 0;
        for(int i = (endIndex - 1); i >= 0; i--) {
            char curr = descBuilder.charAt(i);
            if(curr == '[') {
                descBuilder.deleteCharAt(i);
                arrayIdentifier++;
            } else if(arrayIdentifier > 0) {
                int index = i;
                if(curr == ';') {
                    char temp = curr;
                    while(temp != 'L')
                        temp = descBuilder.charAt(--index);
                }
                if(index > 0 && descBuilder.charAt(index) != '(') {
                    for (int j = 0; j < arrayIdentifier; j++)
                        descBuilder.insert(index, "[");
                    method.desc = descBuilder.toString();
                    break;
                }
            } else break;
        }

    }

    private int removeParameters() {
        /*int paramsRemoved = 0;
        MethodMethodSearcher MethodSearcher = new MethodMethodSearcher();
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                if(method.access != Opcodes.ACC_ABSTRACT && !method.name.contains("<") &&
                        !Modifier.isStatic(method.access)) {
                    //Check both last param, and the one before the last.
                    boolean shouldRemove = true;
                    String[] parameters;
                    try {
                        Type[] types = Type.getArgumentTypes(method.desc);
                        parameters = new String[types.length];
                        for(int j = 0; j < parameters.length; j++)
                            parameters[j] = types[j].toString();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        //System.out.println("["+getName()+"] Caught Parameter Leak, Using Alternate Method.");
                        fixDescription(method);
                        parameters = SearchUtils.getParameters(method);
                    }
                    if(parameters.length > 0) {
                        String lastParam;
                        lastParam = (parameters.length > 1 ?
                                parameters[parameters.length - 2] : parameters[parameters.length - 1]);
                        if (lastParam.equals("B") || lastParam.equals("I") || lastParam.equals("S") || lastParam.equals("Z")) {
                            MethodSearcher.setMethod(method);
                            if (!MethodSearcher.linearMultiSearch(0,
                                    new int[][]{{Opcodes.ALOAD}, {Opcodes.ILOAD}}).isFound())
                                shouldRemove = false;

                            for (int k = 0; k < method.instructions.size(); k++) {
                                if (method.instructions.get(k) instanceof VarInsnNode &&
                                        ((VarInsnNode) method.instructions.get(k)).var == (parameters.length)) {
                                    shouldRemove = false;
                                    break;
                                }
                            }
                            if (shouldRemove) {
                                manipulatedMethods.add(MethodMetaData.create(clazz, method));
                                removeLastParam(method);
                                paramsRemoved++;
                            }
                        }
                    }
                }
            }
        }
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                if(method.access != Opcodes.ACC_ABSTRACT && !method.name.contains("<") &&
                        !Modifier.isStatic(method.access)) {
                    //Check both last param, and the one before the last.
                    boolean shouldRemove = true;
                    String[] parameters;
                    try {
                        Type[] types = Type.getArgumentTypes(method.desc);
                        parameters = new String[types.length];
                        for(int j = 0; j < parameters.length; j++)
                            parameters[j] = types[j].toString();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        //System.out.println("["+getName()+"] Caught Parameter Leak, Using Alternate Method.");
                        fixDescription(method);
                        parameters = SearchUtils.getParameters(method);
                    }
                    if(parameters.length > 0) {
                        String lastParam;
                        lastParam = parameters[parameters.length - 1];
                        if (lastParam.equals("B") || lastParam.equals("I") || lastParam.equals("S")) {
                            MethodSearcher.setMethod(method);
                            if (!MethodSearcher.linearMultiSearch(0,
                                    new int[][]{{Opcodes.ALOAD}, {Opcodes.ILOAD}}).isFound())
                                shouldRemove = false;

                            for (int k = 0; k < method.instructions.size(); k++) {
                                if (method.instructions.get(k) instanceof VarInsnNode &&
                                        ((VarInsnNode) method.instructions.get(k)).var == (parameters.length - 1)) {
                                    shouldRemove = false;
                                    break;
                                }
                            }
                            if (shouldRemove) {
                                manipulatedMethods.add(MethodMetaData.create(clazz, method));
                                removeLastParam(method);
                                paramsRemoved++;
                            }
                        }
                    }
                }
            }
        }
        paramsRemoved += removeInvocationArguments();
        return paramsRemoved;*/
        int fixedParams = 0;
        for (ClassNode classNode : GamePack.getClasses().values()) {
            List<MethodNode> methodList = classNode.methods;
            for (MethodNode Method : methodList) {
                if (Method.access != Opcodes.ACC_ABSTRACT) { //check if its not an empty abstract method
                    if (Method.name.contains("<")) //init,constructor
                        continue;
                    int paramCount;
                    boolean hasDummy = true;
                    Type[] types;
                    try {
                        types = Type.getArgumentTypes(Method.desc);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        fixDescription(Method);
                        types = Type.getArgumentTypes(Method.desc);
                    }
                    paramCount = types.length;
                    String lastParam;
                    if (paramCount > 1) {
                        lastParam = types[paramCount - 2].toString();
                    } else if (paramCount == 1)
                        lastParam = types[paramCount - 1].toString();
                    else
                        continue;
                    if (Modifier.isStatic(Method.access))
                        continue;
                    if (lastParam.equals("B") || lastParam.equals("I") || lastParam.equals("S") || lastParam.equals("Z")) {
                        MethodSearcher Search = new MethodSearcher(Method);
                        //check if there is any mention of the params inside the method
                        int L = Search.linearMultiSearch(0,new int[][]{{Opcodes.ALOAD}, {Opcodes.ILOAD}}).getFirstLine();
                        if (L == -1) //
                            hasDummy = false;
                        //check if there is an instruction fetching the last param
                        for (int I = 0; I < Method.instructions.size(); ++I) {
                            if (Method.instructions.get(I) instanceof VarInsnNode) {
                                if (((VarInsnNode) (Method.instructions.get(I))).var == paramCount) {
                                    hasDummy = false;
                                }
                            }
                        }
                        if (hasDummy) {
                            manipulatedMethods.add(new MethodMetaData(classNode.name, Method.name, Method.desc));
                            removeLastParam(Method);
                            ++fixedParams;
                        }
                    }
                }
            }
        }

        for (ClassNode classNode : GamePack.getClasses().values()) {
            List<MethodNode> methodList = classNode.methods;
            for (MethodNode Method : methodList) {
                if (Method.access != Opcodes.ACC_ABSTRACT) {
                    int paramCount;
                    boolean hasDummy = true;
                    if (Method.name.contains("<"))
                        continue;
                    Type[] types;
                    try {
                        types = Type.getArgumentTypes(Method.desc);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        fixDescription(Method);
                        types = Type.getArgumentTypes(Method.desc);
                    }
                    paramCount = types.length;
                    if (paramCount > 0) {
                        String lastParam = types[paramCount - 1].toString();
                        if (!Modifier.isStatic(Method.access))
                            continue;
                        if (lastParam.equals("B") || lastParam.equals("I") || lastParam.equals("S")) {
                            MethodSearcher Search = new MethodSearcher(Method);
                            int L = Search.linearMultiSearch(0,new int[][]{{Opcodes.ALOAD}, {Opcodes.ILOAD}}).getFirstLine();
                            if (L == -1)
                                hasDummy = false;
                            for (int I = 0; I < Method.instructions.size(); ++I) {
                                if (Method.instructions.get(I) instanceof VarInsnNode) {
                                    if (((VarInsnNode) (Method.instructions.get(I))).var == paramCount - 1) {
                                        hasDummy = false;
                                    }
                                }
                            }
                            if (hasDummy) {
                                manipulatedMethods.add(new MethodMetaData(classNode.name, Method.name, Method.desc));
                                removeLastParam(Method);
                                ++fixedParams;
                            }
                        }
                    }
                }
            }
        }

        fixedParams += removeInvocationArguments();
        return fixedParams;
    }

    private int removeInvocationArguments() {
        int removed = 0;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                for(int i = 0; i < method.instructions.size(); i++) {
                    if (method.instructions.get(i) instanceof MethodInsnNode) {
                        MethodMetaData mmd = new MethodMetaData(clazz.name, ((MethodInsnNode) method.instructions.get(i)).name,
                                ((MethodInsnNode) method.instructions.get(i)).desc);
                        if(manipulatedMethods.contains(mmd)) {
                            removeLastParam(((MethodInsnNode) method.instructions.get(i)));
                            method.instructions.remove(method.instructions.get(i - 1));
                            removed++;
                        }
                    }
                }
            }
        }
        return removed;
    }

    private void removeLastParam(MethodNode Method) {
        String Desc = Method.desc;
        StringBuilder descBuilder = new StringBuilder(Desc);
        int Index = Desc.indexOf(")");
        String c = Character.toString(descBuilder.charAt(Index - 1));
        if (!c.equals(";")) {
            descBuilder.deleteCharAt(Index - 1);
            while(descBuilder.charAt(Index - 1) == '[')
                descBuilder.deleteCharAt(Index - 1);
            Method.desc = descBuilder.toString();
        }
    }

    private void removeLastParam(MethodInsnNode Method) {
        String Desc = Method.desc;
        StringBuilder descBuilder = new StringBuilder(Desc);
        int Index = Desc.indexOf(")");
        descBuilder.deleteCharAt(Index - 1);
        while(descBuilder.charAt(Index - 1) == '[')
           descBuilder.deleteCharAt(Index - 1);
        Method.desc = descBuilder.toString();
    }

    @Override
    public int execute() {
        int totalRemoved = 0;
        int removed = -1;
        int loops = 0;
        while(removed != 0 && loops < 5) {
            loops++;
            removed = removeParameters();
            totalRemoved += removed;
        }
        return totalRemoved;
    }

    @Override
    public String getName() {
        return "Parameters Optimizer";
    }

}
