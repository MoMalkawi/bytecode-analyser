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

public class LocalRequest extends RSClass {

    public LocalRequest() {
        super("LocalRequest", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "I", false),
                high("cache", "#FileCache", false),
                high("bytes", "[B", false),
                high("requester", "#Package", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Node") &&
            SearchUtils.countObjectFields(clazz) == 4 &&
                    Searcher.classContainsFieldDesc(clazz, Utils.formatAsClass(Matchers.
                            getClass("Package").getObfName())))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* id ( I ) */
        FieldNode id = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(id != null)
            insert("id", clazz.name, id.name, id.desc);

        /* bytes ( [B ) */
        FieldNode bytes = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[B"));
        if(bytes != null)
            insert("bytes", clazz.name, bytes.name, bytes.desc);

        /* cache ( #FileCache ) */
        FieldNode cache = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("FileCache").getObfName())));
        if(cache != null)
            insert("cache", clazz.name, cache.name, cache.desc);

        /* requester ( #Package ) */
        FieldNode requester = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("Package").getObfName())));
        if(requester != null)
            insert("requester", clazz.name, requester.name, requester.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

}
