package org.rsminion.classes.impl.collections;

import org.objectweb.asm.tree.*;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;

import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class IterableHashTable extends HashTable {

    public IterableHashTable() {
        super("IterableHashTable");
    }

    @Override
    protected boolean locateClass() {
        nodeDesc = Utils.formatAsClass(Matchers.getClass("Node").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access)

                    && clazz.interfaces.size() == 1

                    && SearchUtils.hasInterfaces(clazz, "java/lang/Iterable")

                    && SearchUtils.countObjectFields(clazz) == 5

                    && Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    i -> !Modifier.isStatic(i.access) && i.desc.equals("I"),
                    ht -> !Modifier.isStatic(ht.access) && ht.desc.equals(nodeDesc),
                    b -> !Modifier.isStatic(b.access) && b.desc.equals("[" + nodeDesc)), 2, 2, 1))
                registerClass(clazz);
        }
        return isFound();
    }

}
