package org.rsminion.classes.impl.io;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class AbstractByteBuffer extends RSClass {

    public AbstractByteBuffer() {
        super("AbstractByteBuffer", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return RSHook.EMPTY_ARRAY;
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
                    Modifier.isAbstract(clazz.access) &&
                    !Modifier.isInterface(clazz.access) &&
                    SearchUtils.countObjectFields(clazz) <= 0 &&
                    SearchUtils.countObjectMethods(clazz) >= 2 &&
                    Searcher.classContainsMethodReturnType(clazz, "[B"))
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
