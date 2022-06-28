package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.data.Pattern;

import java.util.List;

@SuppressWarnings("unchecked")
public class Exceptions extends Deobfuscator {

    private final int[][] EXCEPTION_PATTERNS = {
            {Opcodes.ILOAD, Opcodes.LDC, Pattern.BRANCH_WILDCARD, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW},
            {Opcodes.ILOAD, Pattern.CONST_WILDCARD, Pattern.BRANCH_WILDCARD, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW},
            {Opcodes.ILOAD, Opcodes.ICONST_0, Opcodes.IF_ICMPEQ, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW},
            {Opcodes.ILOAD, Opcodes.ICONST_M1, Opcodes.IF_ICMPNE, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW},
            {Opcodes.ILOAD, Opcodes.ICONST_0, Opcodes.IF_ICMPGT, Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW}
    };

    private int removeExceptions() {
        int removed = 0;
        MethodSearcher searcher = new MethodSearcher();
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                searcher.setMethod(method);
                for(int[] pattern : EXCEPTION_PATTERNS) {
                    int instance = 0;
                    int loops = 0;
                    Pattern result;
                    while((result = searcher.linearSearch(instance, pattern)).isFound()) {
                        loops++;
                        if(method.instructions.get(result.getFirstLine() + 5) instanceof MethodInsnNode) {
                            LabelNode jmp = ((JumpInsnNode) method.instructions.get(result.getFirstLine() + 2)).label;
                            method.instructions.insertBefore(method.instructions.get(result.getFirstLine()),
                                    new JumpInsnNode(Opcodes.GOTO, jmp));
                            for (int j = 0; j < pattern.length; ++j)
                                method.instructions.remove(method.instructions.get(result.getFirstLine() + 1));
                            instance++;
                            removed++;
                        }
                        if(loops > 100) break;
                    }
                }
            }
        }
        return removed;
    }

    @Override
    public int execute() {
        int totalRemoved = 0;
        int removed = -1;
        while(removed != 0) {
            removed = removeExceptions();
            totalRemoved += removed;
        }
        return totalRemoved;
    }

    @Override
    public String getName() {
        return "Exceptions Remover";
    }

}
