package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.rsminion.core.gamepack.GamePack;

import java.util.List;
import java.util.ListIterator;

//Jump Optimizer From asm.ow2.io documentation

@SuppressWarnings("unchecked")
public class Jumps extends Deobfuscator {

    private int optimizeJumps() {
        int optimized = 0;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode ain = iterator.next();

                    if(ain instanceof JumpInsnNode) {
                        LabelNode lbl = ((JumpInsnNode) ain).label;
                        AbstractInsnNode tgt;

                        while(true) {
                            tgt = lbl;
                            while (tgt != null && tgt.getOpcode() < 0){
                                tgt = tgt.getNext();
                            }
                            if(tgt != null && tgt.getOpcode() == Opcodes.GOTO)
                                lbl = ((JumpInsnNode)tgt).label;
                            else break;
                        }

                        ((JumpInsnNode) ain).label = lbl;

                        if(ain.getOpcode() == Opcodes.GOTO && tgt != null) {
                            int opcode = tgt.getOpcode();

                            if((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) ||
                                    opcode == Opcodes.ATHROW) {
                                method.instructions.set(ain, tgt.clone(null));
                                optimized++;
                            }
                        }
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
            optimized = optimizeJumps();
            totalOptimized += optimized;
        }
        return totalOptimized;
    }

    @Override
    public String getName() {
        return "Jumps Optimizer";
    }

}


