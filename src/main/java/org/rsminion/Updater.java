package org.rsminion;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.gamepack.GamePackHandler;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.Result;
import org.rsminion.core.multipliers.MultiplierCache;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.MultiplierSearcher;
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.Logger;

import java.util.List;

public class Updater {

    public static void main(String[] args) {
        Logger.info("[Updater] Starting Preparation...");
        /* GamePack Preparation */
        Logger.info("[Updater] Starting GamePackHandler...");
        GamePackHandler.init();
        Logger.info("[Updater] GamePack Ready for Matching.");
        /* Multipliers */
        Logger.info("[Updater] Starting MultiplierSearcher...");
        Logger.info("[Updater] Cached " + MultiplierSearcher.search() + " Multipliers.");
        MultiplierCache.filterMultipliers();
        Logger.info("[Updater] Filtered MultiplierCache.");
        Logger.info("[Updater] Finished Preparation.");
        /* Pattern Matching */
        Logger.info("[Updater] Starting Pattern Matching...");
        Matchers.execute();
        Logger.info("[Updater] Finished Pattern Matching.");
    }

    @SuppressWarnings("unchecked, unused")
    private static void test() {
        int totalCount = 0;
        MethodSearcher ms = new MethodSearcher();
        for(ClassNode clazz : GamePack.getClasses().values()) {
            List<MethodNode> methods = clazz.methods;
            for(MethodNode method : methods) {
                ms.setMethod(method);
                Pattern result;
                int instance = 0;
                int loop = 0;
                while((result = ms.linearSearch(instance,
                        Pattern.GET_WILDCARD, Opcodes.LDC, Opcodes.ALOAD,  Pattern.MUL_WILDCARD
                )).isFound()) {
                    if(loop > 100) break;
                    Logger.info("Class: "+clazz+" Method: "+method+" Line: "+result.getFirstLine());
                    totalCount++;
                    instance++;
                    loop++;
                }
            }
        }
        Logger.info("Total: "+totalCount);
    }

}