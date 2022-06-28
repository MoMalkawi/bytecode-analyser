package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.data.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class Multipliers extends Deobfuscator {

    private final int[][] MULTIPLIERS_PATTERNS = {
            {Opcodes.LDC, Opcodes.ALOAD, Pattern.GET_WILDCARD, Pattern.MUL_WILDCARD},
            {Opcodes.LDC, Opcodes.ALOAD, Pattern.LOAD_WILDCARD, Pattern.MUL_WILDCARD},
            {Opcodes.LDC, Pattern.LOAD_WILDCARD, Pattern.MUL_WILDCARD},
            {Opcodes.LDC, Pattern.GET_WILDCARD, Pattern.MUL_WILDCARD},
            {Opcodes.LDC, Opcodes.GETSTATIC, Opcodes.ILOAD, Opcodes.AALOAD, Opcodes.GETFIELD, Pattern.MUL_WILDCARD}
    };

    private int optimizeMultipliers() {
        int optimized = 0;
        MethodSearcher searcher = new MethodSearcher();
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                searcher.setMethod(method);
                List<AbstractInsnNode> instructions = new ArrayList<>(Arrays.asList(method.
                        instructions.toArray()));
                for(int[] pattern : MULTIPLIERS_PATTERNS) {
                    Pattern result;
                    int instance = 0;
                    int insertionOffset;
                    while((result = searcher.linearSearch(instance, pattern)).isFound()) {

                        if(method.instructions.get(result.getFirstLine() + 1) instanceof FieldInsnNode)
                            insertionOffset = pattern.length == 6 ? 5 : 2; //LDC, GET, etc...
                        else insertionOffset = 3; //LDC, ALOAD, etc..

                        instructions.add(result.getFirstLine() + insertionOffset,
                                instructions.get(result.getFirstLine()));
                        instructions.remove(result.getFirstLine());

                        optimized++;
                        instance++;
                    }
                }

                method.instructions.clear();
                for(AbstractInsnNode ain : instructions) method.instructions.add(ain);
            }
        }
        return optimized;
    }

    private void rearrangeFields(MethodNode method, Pattern pattern) {

    }

    @Override
    public int execute() {
        int totalOptimized = 0;
        int optimized = -1;
        while(optimized != 0) {
            optimized = optimizeMultipliers();
            totalOptimized += optimized;
        }
        return totalOptimized;
    }

    @Override
    public String getName() {
        return "Multiplier Optimizer";
    }

}
