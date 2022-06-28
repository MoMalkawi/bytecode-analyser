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

public class Queue extends RSClass {

    private String cacheNodeDesc;

    public Queue() {
        super("Queue", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("head", "#CacheNode", false)
        };
    }

    @Override
    protected boolean locateClass() {
        cacheNodeDesc = Utils.formatAsClass(Matchers.getClass("CacheNode").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

            SearchUtils.hasInterfaces(clazz, "java/lang/Iterable") &&

            SearchUtils.countObjectFields(clazz) == 2 &&

            Searcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals(cacheNodeDesc), clazz) != null)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        /* head ( #CacheNode ) */
        FieldNode head = Searcher.findField(h -> !Modifier.isStatic(h.access) &&
                h.desc.equals(cacheNodeDesc), clazz);
        if(head != null)
            insert("head", clazz.name, head.name, head.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "CacheNode" };
    }

}
