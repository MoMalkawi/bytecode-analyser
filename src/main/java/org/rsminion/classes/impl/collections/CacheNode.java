package org.rsminion.classes.impl.collections;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

//EntityNode
@SuppressWarnings("unchecked")
public class CacheNode extends Node {

    public CacheNode() {
        super("CacheNode");
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "J", false),
                high("next", "#CacheNode", false),
                high("previous", "#CacheNode", false)
        };
    }

    @Override
    protected boolean locateClass() {
        int fieldCount;
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Node") &&
                    (fieldCount = SearchUtils.countObjectFields(clazz)) >= 2 && fieldCount <= 3) {
                if(Searcher.countFieldNodes(clazz,
                        c -> !Modifier.isStatic(c.access) &&
                                c.desc.equals(Utils.formatAsClass(clazz.name)))[0] == 2)
                    registerClass(clazz);
            }
        }
        return isFound();
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

}
