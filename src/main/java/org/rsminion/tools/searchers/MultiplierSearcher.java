package org.rsminion.tools.searchers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.multipliers.MultiplierCache;
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.Logger;

import java.util.List;

@SuppressWarnings("unchecked")
public class MultiplierSearcher {

    private static final int[][] MULTIPLIER_PATTERNS = {
            {Pattern.GET_WILDCARD, Opcodes.LDC, Pattern.MUL_WILDCARD},
            {Pattern.GET_WILDCARD, Opcodes.ILOAD, Opcodes.AALOAD, Pattern.GET_WILDCARD,
                    Opcodes.LDC, Pattern.MUL_WILDCARD},
    };

    public static int search() {
        Logger.info("[MultiplierSearcher] Cycling through classes...");
        int multipliersCollected = 0;
        MethodSearcher searcher = new MethodSearcher();
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                searcher.setMethod(method);
                for(int[] pattern : MULTIPLIER_PATTERNS) {

                    Pattern result;
                    int instance = 0;
                    int loop = 0;
                    while((result = searcher.linearSearch(instance, pattern)).isFound()) {
                        if(loop > 100) break;
                        registerMultiplier(result, method);
                        multipliersCollected++;
                        loop++;
                        instance++;
                    }

                }
            }
        }
        return multipliersCollected;
    }

    private static void registerMultiplier(Pattern pattern, MethodNode method) {
        Number multiplier = null;
        Number multiKey = null;
        FieldInsnNode fin;
        AbstractInsnNode[] nodes = pattern.getAll();
        for(AbstractInsnNode ain : nodes) {
            if(ain.getOpcode() == Opcodes.GETFIELD || ain.getOpcode() == Opcodes.GETSTATIC) {
                fin = (FieldInsnNode) ain;
                multiKey = MultiplierCache.toKey(fin.owner, fin.name);
            }
            if(ain instanceof LdcInsnNode)
                multiplier = (Number) ((LdcInsnNode)ain).cst;
        }
        if(multiKey != null && multiplier != null) {
            MultiplierCache.insert(multiKey.intValue(), multiplier);
            AbstractInsnNode potentialPut = method.instructions.get(pattern.getLastLine() + 1);
            if(potentialPut.getOpcode() == Opcodes.PUTSTATIC) {
                fin = (FieldInsnNode) potentialPut;
                MultiplierCache.insert(MultiplierCache.toKey(fin.owner, fin.name), multiplier);
            }
        }
    }

}
