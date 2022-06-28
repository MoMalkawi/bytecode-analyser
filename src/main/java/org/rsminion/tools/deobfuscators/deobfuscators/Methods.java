package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.tools.searchers.data.MethodMetaData;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Methods extends Deobfuscator {

    private final List<MethodMetaData> allMethods = new ArrayList<>();
    private final Set<MethodMetaData> whiteListedMethods = new HashSet<>();

    private int removeMethods() {
        filterGamePack();
        int methodsRemoved = 0;
        List<MethodMetaData> badMethods = getBadMethods();
        MethodNode methodNode;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            for(MethodMetaData method : badMethods) {
                if(method.getOwner().equals(clazz.name)) {
                    if ((methodNode = Searcher.findMethod(m -> m.name.equals(method.getName()) &&
                            m.desc.equals(method.getDescription()), clazz)) != null) {
                        clazz.methods.remove(methodNode);
                        methodsRemoved++;
                    }
                }
            }
        }
        return methodsRemoved;
    }

    private void filterGamePack() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            whiteListInterfaces(clazz);
            whiteListInvoked(clazz);
            whiteListNative(clazz);
        }
    }

    //Overridden & Abstract Methods aren't dummy.
    private void whiteListNative(ClassNode clazz) {
        List<MethodNode> methods = clazz.methods;
        for(MethodNode method : methods) {
            allMethods.add(MethodMetaData.create(clazz, method));
            if(method.name.length() > 2 || Modifier.isAbstract(method.access) ||
                    SearchUtils.isOverridden(clazz, method))
                whiteList(MethodMetaData.create(clazz, method));
        }
    }

    //All Used Interfaces aren't dummy.
    private void whiteListInterfaces(ClassNode clazz) {
        List<String> interfacesNames = clazz.interfaces;
        if(interfacesNames.size() > 0) {
            for(String interfaceName : interfacesNames) {
                if(!interfaceName.contains("java")) {
                    ClassNode interfaceClass = GamePack.get(interfaceName);
                    List<MethodNode> methods = interfaceClass.methods;
                    for(MethodNode method : methods)
                        whiteList(MethodMetaData.create(clazz, method));
                }
            }
        }
    }

    //Invoked Methods aren't dummy.
    private void whiteListInvoked(ClassNode clazz) {
        List<MethodNode> methods = clazz.methods;
        MethodInsnNode min;
        for(MethodNode method : methods) {
            AbstractInsnNode[] instructions = method.instructions.toArray();
            for(AbstractInsnNode instruction : instructions) {
                if(instruction instanceof MethodInsnNode) {
                    min = (MethodInsnNode) instruction;
                    if(!min.owner.contains("java")) {
                        //Check direct super
                        ClassNode superClass = GamePack.get(min.owner);
                        if(Searcher.classContainsMethod(superClass, min.name, min.desc))
                            whiteList(MethodMetaData.create(min));
                        else {
                            //Climb up the hierarchy until it finds the method, else continue...
                            String superName;
                            while(!(superName = superClass.superName).contains("java")) {
                                superClass = GamePack.get(superClass.superName);
                                if(Searcher.classContainsMethod(superClass, min.name, min.desc)) {
                                    whiteList(new MethodMetaData(superName, min.name, min.desc));
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private List<MethodMetaData> getBadMethods() {
        List<MethodMetaData> result = new ArrayList<>();
        for(MethodMetaData mmd : allMethods) {
            if(!isWhiteListed(mmd))
                result.add(mmd);
        }
        return result;
    }

    @Override
    public int execute() {
        int totalRemoved = 0;
        int removed = -1;
        while(removed != 0) {
            removed = removeMethods();
            totalRemoved += removed;
        }
        return totalRemoved;
    }

    private void whiteList(MethodMetaData method) {
        if(!isWhiteListed(method))
            whiteListedMethods.add(method);
    }

    private boolean isWhiteListed(MethodMetaData method) {
        return whiteListedMethods.contains(method);
    }

    @Override
    public String getName() {
        return "Fake Methods Remover";
    }

}
