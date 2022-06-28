package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.data.Pattern;

import java.util.List;

@SuppressWarnings("unchecked")
public class Predicates extends Deobfuscator {

    private final int[][] PREDICATE_PATTERNS = {
            {Opcodes.ILOAD, Pattern.CONST_WILDCARD, Pattern.BRANCH_WILDCARD, Opcodes.RETURN},
            {Opcodes.ILOAD, Opcodes.LDC, Pattern.BRANCH_WILDCARD, Opcodes.RETURN}
    };

    private int optimizePredicates() {
        int optimized = 0;
        MethodSearcher searcher = new MethodSearcher();
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                searcher.setMethod(method);
                for(int[] pattern : PREDICATE_PATTERNS) {
                    Pattern result;
                    int instance = 0;
                    while((result = searcher.linearSearch(instance, pattern)).isFound()) {
                        LabelNode jmp = ((JumpInsnNode) method.instructions.get(result.getFirstLine()
                                + 2)).label;
                        method.instructions.insertBefore(method.instructions.get(result.getFirstLine()),
                                new JumpInsnNode(Opcodes.GOTO, jmp));
                        for (int j = 1; j < pattern.length; ++j)
                            method.instructions.remove(method.instructions.get(result.getFirstLine() + 1));
                        optimized++;
                        instance++;
                    }
                }
            }
        }
        return optimized;
    }

    @Override
    public int execute() {
        int totalOptimized = 0;
        int optimized = -1;
        while(optimized != 0) {
            optimized = optimizePredicates();
            totalOptimized += optimized;
        }
        return totalOptimized;
    }

    @Override
    public String getName() {
        return "Predicates Optimizer";
    }

}
