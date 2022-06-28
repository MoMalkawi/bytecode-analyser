package org.rsminion.classes.impl.collections;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;

import java.lang.reflect.Modifier;

public class ObjectNode extends RSClass {

    public ObjectNode() {
        super("ObjectNode", Matchers.Importance.LOW);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("value", "Ljava/lang/Object;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Node") &&

            !Modifier.isFinal(clazz.access) &&

            SearchUtils.countObjectFields(clazz) == 1 &&

            //SearchUtils.countObjectMethods(clazz) == 1 &&

            Searcher.findField(f -> !Modifier.isStatic(f.access) &&
                    SearchUtils.isNativeObject(f.desc), clazz) != null)
                registerClass(clazz);

        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        /* value ( Ljava/lang/Object; ) */
        FieldNode value = Searcher.findField(f -> !Modifier.isStatic(f.access) && SearchUtils.isNativeObject(f.desc),
                clazz);
        if(value != null)
            insert("value", clazz.name, value.name, value.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

}
