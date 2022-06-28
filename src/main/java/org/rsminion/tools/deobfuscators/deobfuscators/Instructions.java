package org.rsminion.tools.deobfuscators.deobfuscators;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.tools.utils.Logger;

import java.util.List;

//DeadCode Remover From asm.ow2.io documentation

@SuppressWarnings("unchecked")
public class Instructions extends Deobfuscator {

    private int removeDeadCode() {
        int instructionsRemoved = 0;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            instructionsRemoved++;;
            Analyzer analyzer = new Analyzer(new BasicInterpreter());
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                try {

                    Frame[] analyzerFrames = analyzer.analyze(clazz.name, method);
                    for(int i = 0; i < analyzerFrames.length; i++) {
                        if(analyzerFrames[i] == null && !(method.instructions.get(i) instanceof LabelNode)) {
                            method.instructions.remove(method.instructions.get(i));
                            instructionsRemoved++;;
                        }
                    }

                } catch (AnalyzerException e) {
                    Logger.info("[ASM-Analyser] Warning: " + e.getMessage() + " " + clazz.name +"/" + method.name);
                }
            }
        }
        return instructionsRemoved;
    }

    @Override
    public int execute() {
        int totalRemoved = 0;
        int removed = -1;
        int loops = 0;
        while(removed != 0 && loops < 5) {
            removed = removeDeadCode();
            totalRemoved += removed;
            loops++;
        }
        return totalRemoved;
    }

    @Override
    public String getName() {
        return "Fake Instructions Remover";
    }

}
