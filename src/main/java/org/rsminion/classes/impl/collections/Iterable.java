package org.rsminion.classes.impl.collections;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class Iterable extends RSClass {

    public Iterable() {
        super("Iterable", Matchers.Importance.HIGH); //Check importance later.
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
            high("node", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(clazz.interfaces.size() > 1 &&

                    SearchUtils.hasInterfaces(clazz,
                    "java/lang/Iterable", "java/util/Collection")

                    && SearchUtils.countObjectFields(clazz) < 3

                    && Searcher.countFieldNodes(clazz, n -> !Modifier.isStatic(n.access) &&
                        n.desc.equals(Utils.formatAsClass(Matchers.getClass("Node").
                                getObfName())))[0] > 0)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        MethodNode constructor = getConstructor();
        //Insert Constructor with Real Class Name
        insert(name, constructor);
        if(constructor != null) {

            /* Node */
            Pattern pattern = new MethodSearcher(constructor).
                    linearSearch(0, Opcodes.ALOAD, Opcodes.GETFIELD);
            if(pattern.isFound())
                insert("node", (FieldInsnNode) pattern.get(1));

        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

}
