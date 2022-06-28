package org.rsminion.classes.impl.io;

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

@SuppressWarnings("unchecked")
public class Buffer extends RSClass { //Stream

    public Buffer() {
        super("Buffer", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("bytes", "[B", false),
                high("offset", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Node") &&
            SearchUtils.countObjectFields(clazz) == 2 &&
            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    ba -> !Modifier.isStatic(ba.access) && ba.desc.equals("[B"),
                    i -> !Modifier.isStatic(i.access) && i.desc.equals("I")), 1, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* bytes ( [B ) */
        FieldNode bytes = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[B"));
        if(bytes != null)
            insert("bytes", clazz.name, bytes.name, bytes.desc);

        /* offset ( I ) */
        FieldNode offset = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(offset != null)
            insert("offset", clazz.name, offset.name, offset.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }
}
