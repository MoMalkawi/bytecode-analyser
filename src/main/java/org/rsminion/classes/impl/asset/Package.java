package org.rsminion.classes.impl.asset;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

//TODO: do fields if needed in the future.
public class Package extends RSClass {

    public Package() {
        super("Package", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return RSHook.EMPTY_ARRAY;
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "AbstractPackage") &&
            SearchUtils.countObjectFields(clazz) >= 6)
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
