package org.rsminion.classes.impl.io;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

public class CacheWorker extends RSClass {

    public CacheWorker() {
        super("CacheWorker", Matchers.Importance.LOW);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return RSHook.EMPTY_ARRAY;
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.hasInterfaces(clazz, "java/lang/Runnable") &&
            SearchUtils.countObjectFields(clazz) <= 0)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {}

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }

}
