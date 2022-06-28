package org.rsminion.core.matching.data;

import org.rsminion.classes.RSClass;
import org.rsminion.core.matching.Matchers;
import org.rsminion.tools.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class Result {

    private final List<RSHook> mismatchedHooks = new ArrayList<>();

    private final List<RSHook> missingHooks = new ArrayList<>();

    private final List<RSClass> missingClasses = new ArrayList<>();

    private final List<RSClass> availableClasses = new ArrayList<>();

    public static Result create() {
        Result result = new Result();
        for(RSClass clazz : Matchers.getClasses().values()) {
            if(clazz.isFound()) {

                result.availableClasses.add(clazz);
                for(RSHook hook : clazz.getHooks().values()) {
                    if(!hook.isFound())
                        result.missingHooks.add(hook);
                    else if(hook.isMismatched())
                        result.mismatchedHooks.add(hook);
                }

            } else result.missingClasses.add(clazz);
        }
        return result;
    }

    public void print(boolean missingClasses,
                      boolean missingHooks,
                      boolean mismatchedHooks,
                      boolean availableClasses,
                      boolean availableHooks) {
        if(availableClasses || availableHooks) {
            Logger.info("================ Classes/Hooks ================");
            System.out.println();
            for (RSClass clazz : this.availableClasses) {
                if(availableClasses) System.out.println(formatClassForPrint(clazz));
                if(availableHooks) {
                    for (RSHook hook : clazz.getHooks().values()) {
                        if (hook.isFound() && !hook.isMismatched())
                            System.out.println(formatHookForPrint(hook));
                    }
                }
                System.out.println();
            }
        }
        Logger.info("================ Errors/Mismatches ================");
        System.out.println();
        if(missingClasses && this.missingClasses.size() > 0) {
            for(RSClass clazz : this.missingClasses)
                System.out.println("[Missing] " + formatClassForPrint(clazz));
            System.out.println();
        }
        if(missingHooks && this.missingHooks.size() > 0) {
            for(RSHook hook : this.missingHooks)
                System.out.println("[Missing] " + formatHookForPrintError(hook));
            System.out.println();
        }
        if(mismatchedHooks && this.mismatchedHooks.size() > 0) {
            for(RSHook hook : this.mismatchedHooks)
                System.out.println("[Mismatch] " + formatHookForPrint(hook));
            System.out.println();
        }
    }

    private String formatClassForPrint(RSClass clazz) {
        return "[Class] " + clazz.getName() + " as: " + clazz.getObfName() + " ["+clazz.getImportance().name()+"]";
    }

    private String formatHookForPrint(RSHook hook) {
        return "[Hook] " + hook.getName() + " as: " + hook.getObfOwner() + "/" + hook.getObfName() + " (" + hook.getMultiplier() +
                ") ----- Desc: " + formatDesc(hook.getDesc());
    }

    private String formatHookForPrintError(RSHook hook) {
        return "[Hook] " + hook.getHookHolder() + "(" + hook.getName() + ") (" +hook.getDescRequired() + ") [" + hook.getImportance().name()+"]";

    }

    private String formatDesc(String desc) {
        if(desc != null && desc.contains(";")) {
            int classIndex = desc.indexOf("L");
            RSClass clazz = Matchers.getClass(desc.substring(classIndex + 1, desc.indexOf(";")), true);
            if(clazz != null) {
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < classIndex; i++)
                    sb.append("[");
                sb.append(clazz.getName());
                return sb.toString();
            }
        }
        return desc;
    }

    public void createHooksFile() {

    }

}
