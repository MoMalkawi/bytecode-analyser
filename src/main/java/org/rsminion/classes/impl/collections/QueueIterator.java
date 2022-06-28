package org.rsminion.classes.impl.collections;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class QueueIterator extends RSClass {

    private String iterableQueueDesc;
    private String nodeDesc;

    public QueueIterator() {
        super("QueueIterator", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("queue", "#IterableQueue", false),
                high("current", "#Node", false),
                high("previous", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        iterableQueueDesc = Utils.formatAsClass(Matchers.getClass("IterableQueue").getObfName());
        nodeDesc = Utils.formatAsClass(Matchers.getClass("Node").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

            SearchUtils.hasInterfaces(clazz, "java/util/Iterator") &&

            SearchUtils.countObjectFields(clazz) == 3 &&

            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    iq -> !Modifier.isStatic(iq.access) && iq.desc.equals(iterableQueueDesc),
                    n -> !Modifier.isStatic(n.access) && n.desc.equals(nodeDesc)), 1, 2))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override //previous is set as NULL at object init. <- alternative if this breaks.
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode next = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                m.name.equals("next") && SearchUtils.isReturnType(m, "Ljava/lang/Object;"));
        //If Jagex's obfuscator decides to change its name
        if(next == null)
            next = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    SearchUtils.isReturnType(m, "Ljava/lang/Object;"));

        if(next != null) {
            insert("next", next);

            MethodSearcher methodSearcher = new MethodSearcher(next);

            /* current ( #Node ) */
            Pattern current = methodSearcher.singularSearch(f -> ((FieldInsnNode)f).owner.equals(clazz.name),
                    0, Opcodes.GETFIELD);

            if(current.isFound()) {
                insert("current", (FieldInsnNode) current.getFirst());

                /* previous ( #Node ) */
                FieldNode previous = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals(nodeDesc) && !isHookFound(f.name, true));
                if(previous != null)
                    insert("previous", clazz.name, previous.name, previous.desc);

            }

        }

        /* queue ( #IterableQueue ) */
        FieldNode queue = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(iterableQueueDesc));
        if(queue != null)
            insert("queue", clazz.name, queue.name, queue.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "IterableQueue" };
    }

}
