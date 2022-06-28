package org.rsminion.classes.impl.devices;

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
public class MouseTracker extends RSClass {

    public MouseTracker() {
        super("MouseTracker", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("tracking", "Z", false),
                high("length", "I", false),
                high("lock", "Ljava/lang/Object;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(!Modifier.isAbstract(clazz.access) && !Modifier.isFinal(clazz.access) &&
                    SearchUtils.isStandaloneObject(clazz) &&
            clazz.interfaces.size() == 1 && Searcher.countFieldNodes(clazz,
                    o -> !Modifier.isStatic(o.access) && o.desc.equals("Ljava/lang/Object;"))[0] == 1)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* lock ( Ljava/lang/Object; ) */
        FieldNode lock = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/lang/Object;"));
        if(lock != null)
            insert("lock", clazz.name, lock.name, lock.desc);

        /* length ( I ) */
        FieldNode length = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(length != null)
            insert("length", clazz.name, length.name, length.desc);

        /* tracking ( Z ) */
        FieldNode tracking = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Z"));
        if(tracking != null)
            insert("tracking", clazz.name, tracking.name, tracking.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }
}
