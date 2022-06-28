package org.rsminion.classes.impl.collections;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class LinearHashTable extends RSClass {

    public LinearHashTable() {
        super("LinearHashTable", Matchers.Importance.LOW); //TODO: check importance
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("values", "[I", false) //TODO: check importance
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz)
            && SearchUtils.countObjectFields(clazz) == 1
            && !Modifier.isFinal(clazz.access)
            && Searcher.findField(f -> !Modifier.isStatic(f.access) && f.desc.equals("[I"), clazz) != null)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        /* Values ( [I ) */
        FieldNode values = Searcher.findField(f -> !Modifier.isStatic(f.access) && f.desc.equals("[I"),
                clazz);
        if(values != null)
            insert("values", clazz.name, values.name, values.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }
}
