package org.rsminion.core.gamepack;

import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.tools.searchers.Searcher;

import java.util.Arrays;
import java.util.Map;

public class GamePack {

    private static int revision = -1;

    private @Getter @Setter static Map<String, ClassNode> classes;

    public static boolean has(String className) {
        return classes.containsKey(className);
    }

    public static ClassNode get(String className) {
        return classes.get(className);
    }

    public static int getRevision() {
        if(revision != -1) return revision;

        ClassNode client = classes.get("client");
        if(client != null) {
            MethodNode constructor = Searcher.findMethod(m -> m.name.equals("init"), client);
            if(constructor != null) {
                Arrays.stream(constructor.instructions.toArray()).filter(i -> i instanceof IntInsnNode &&
                        ((IntInsnNode)i).operand == 503).findFirst().
                        ifPresent(i -> revision = ((IntInsnNode)i.getNext()).operand);
            }
        }
        return revision;
    }

}