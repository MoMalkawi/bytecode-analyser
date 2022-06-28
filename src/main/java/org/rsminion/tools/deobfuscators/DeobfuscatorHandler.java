package org.rsminion.tools.deobfuscators;

import org.rsminion.tools.deobfuscators.deobfuscators.*;
import org.rsminion.tools.utils.Logger;

public class DeobfuscatorHandler {

    private static final Deobfuscator[] all =
            {
                    new Methods(),
                    new Instructions(),
                    new Multipliers(),
                    new ArithmeticOperations(), //Re-write
                    new Jumps(),
                    new Exceptions(),
                    new Predicates(),
                    new Parameters()
            };

    public static void run() {
        Logger.info("[Deobfuscator] Starting Deobfuscation...");
        for(Deobfuscator deobfuscator : all) {
            Logger.info("[" + deobfuscator.getName() + "] Completed " +
                    deobfuscator.execute() + " Operations.");
        }
        Logger.info("[Deobfuscator] Completed Tasks.");
    }

}
