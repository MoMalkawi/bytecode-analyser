package org.rsminion.classes.impl.scene.actor;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class NPC extends RSClass {

    public NPC() {
        super("NPC", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
            high("composite", "#NpcComposite", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
                    SearchUtils.isParent(clazz, "Actor") &&
                    Utils.isBetween(SearchUtils.countObjectFields(clazz), 0, 3))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* composite ( #NpcComposite ) */
        FieldNode composite = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.startsWith("L") && f.desc.endsWith(";"));
        if(composite != null)
            insert("composite", clazz.name, composite.name, composite.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Actor" };
    }

}
