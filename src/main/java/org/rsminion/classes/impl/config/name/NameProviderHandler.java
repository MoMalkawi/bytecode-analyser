package org.rsminion.classes.impl.config.name;

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
public class NameProviderHandler extends RSClass { //from my old updater

    public NameProviderHandler() {
        super("NameProviderHandler", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("nameProviders", "[#NameProvider", false),
                high("size", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) && Modifier.isAbstract(clazz.access) &&
                    Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                            i -> !Modifier.isFinal(i.access) && !Modifier.isStatic(i.access) && i.desc.equals("I"),
                            h -> !Modifier.isFinal(h.access) && !Modifier.isStatic(h.access) && h.desc.equals("Ljava/util/HashMap;")
                    ), 1, 2))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* size ( I ) */
        FieldNode size = classSearcher.findField(f -> !Modifier.isPublic(f.access) &&
                !Modifier.isFinal(f.access) && !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(size != null)
            insert("size", clazz.name, size.name, size.desc);

        /* nameProviders ( [#NameProvider ) */
        FieldNode nameProviders = classSearcher.findField(f -> !Modifier.isPublic(f.access) &&
                !Modifier.isFinal(f.access) && !Modifier.isStatic(f.access) &&
                f.desc.equals(String.format("[L%s;", Matchers.getObfClass("NameProvider"))));
        if(nameProviders != null)
            insert("nameProviders", clazz.name, nameProviders.name, nameProviders.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "NameProvider" };
    }
}
