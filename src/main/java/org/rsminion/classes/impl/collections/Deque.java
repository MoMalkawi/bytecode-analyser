package org.rsminion.classes.impl.collections;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
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
public class Deque extends RSClass { //LinkedList

    private String nodeDesc;

    public Deque() {
        super("Deque", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("head", "#Node", false),
                high("current", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        nodeDesc = Utils.formatAsClass(Matchers.getClass("Node").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

                    !Modifier.isFinal(clazz.access) &&

                    SearchUtils.countObjectFields(clazz) == 2 &&

                    !SearchUtils.hasInterfaces(clazz, "java/lang/Iterable") &&

                    Searcher.countFieldNodes(clazz, f -> !Modifier.isStatic(f.access) &&
                            f.desc.equals(nodeDesc))[0] == 2)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        MethodNode constructor = getConstructor();
        if(constructor != null) {
            insert("Deque", constructor);

            /* head ( #Node ) */
            Pattern head = new MethodSearcher(constructor).singularSearch(h -> ((FieldInsnNode)h).owner.
                    equals(clazz.name), 0, Opcodes.GETFIELD);
            if(head.isFound()) {
                insert("head", (FieldInsnNode) head.getFirst());

                /* current ( #Node ) */
                FieldNode current = Searcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals(nodeDesc) && !isHookFound(f.name, true), clazz);
                if(current != null)
                    insert("current", clazz.name, current.name, current.desc);
            }
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

}
