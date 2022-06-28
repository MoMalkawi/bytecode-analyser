package org.rsminion.classes.impl.asset;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class RemoteRequest extends RSClass {

    public RemoteRequest() {
        super("RemoteRequest", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("padding", "B", false),
                high("pack", "#Package", false),
                high("checksum", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "CacheNode") &&
            SearchUtils.countObjectFields(clazz) == 3 &&
            Searcher.classContainsFieldDesc(clazz, Utils.formatAsClass(Matchers.
                    getClass("Package").getObfName())))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* padding ( B ) */
        FieldNode padding = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("B"));
        if(padding != null)
            insert("padding", clazz.name, padding.name, padding.desc);

        /* checksum ( I ) */
        FieldNode checksum = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(checksum != null)
            insert("checksum", clazz.name, checksum.name, checksum.desc);

        /* pack ( I ) */
        FieldNode pack = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("Package").getObfName())));
        if(pack != null)
            insert("pack", clazz.name, pack.name, pack.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "CacheNode", "Package" };
    }

}
