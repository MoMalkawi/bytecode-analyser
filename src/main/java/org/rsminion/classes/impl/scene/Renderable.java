package org.rsminion.classes.impl.scene;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;

import java.lang.reflect.Modifier;

public class Renderable extends RSClass {

    public Renderable() {
        super("Renderable", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
            high("modelHeight", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "CacheNode") &&
            Modifier.isAbstract(clazz.access) &&
            SearchUtils.countObjectFields(clazz) == 1 &&
            Searcher.findMethod(f-> f.name.equals("<init>") && Modifier.isProtected(f.access),
                    clazz) != null)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        /* modelHeight ( I ) */
        FieldNode modelHeight = new ClassSearcher(clazz).findField(f ->
                !Modifier.isStatic(f.access) && f.desc.equals("I"));
        if(modelHeight != null)
            insert("modelHeight", clazz.name, modelHeight.name, modelHeight.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "CacheNode" };
    }

}
