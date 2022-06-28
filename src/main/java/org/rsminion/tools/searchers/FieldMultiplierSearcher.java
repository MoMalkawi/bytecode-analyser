package org.rsminion.tools.searchers;

import lombok.AllArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.data.Pattern;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked, unused")
@AllArgsConstructor
@Deprecated
public class FieldMultiplierSearcher {

    private RSHook hook;

    private final Set<Number> multipliers = new HashSet<>();

    private final MethodSearcher searcher = new MethodSearcher();

    private void locateMultipliers() {
        int[][] patternsToSearch = {getPattern()};
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                searcher.setMethod(method);
                for(int[] pattern : patternsToSearch)
                    registerMultiplier(pattern);
            }
        }
    }

    private void registerMultiplier(int[] pattern) {
        Pattern result;
        int instance = 0;
        int loops = 0;
        while((result = searcher.linearSearch(instance, pattern)).isFound()) {
            if(loops > 100) break;

            List<FieldInsnNode> fins = result.getFieldNodes();
            if(fins.stream().anyMatch(f -> f.owner.equals(hook.getObfOwner()) &&
                    f.name.equals(hook.getObfName()))) {
                LdcInsnNode ldcNode = (LdcInsnNode) result.getFirstNodeByOpcode(Opcodes.LDC);
                if (ldcNode != null) multipliers.add((Number) ldcNode.cst);
            }
            instance++;
            loops++;
        }
    }

    private int[] getPattern() {
        return new int[] {(hook.isStaticField() ? Opcodes.GETSTATIC : Opcodes.GETFIELD), Opcodes.LDC, getMul()};
    }

    //TODO
    public String get() {
        return null; //mode, etc..
    }

    private int getMul() {
        switch (hook.getDesc()) {
            case "I": return Opcodes.IMUL;
            case "J": return Opcodes.LMUL;
            case "D": return Opcodes.DMUL;
            case "F": return Opcodes.FMUL;
        }
        return -1;
    }

    private boolean hasMultiplier() {
        return "IDJF".contains(hook.getDesc());
    }

    public Set<Number> getAll() {
        return multipliers;
    }

    public static FieldMultiplierSearcher locate(RSHook hook) {
        FieldMultiplierSearcher ms = new FieldMultiplierSearcher(hook);
        if(ms.hasMultiplier()) ms.locateMultipliers();
        return ms;
    }

}
