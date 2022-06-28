package org.rsminion.classes.impl.collections;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
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
public class HashTableIterator extends RSClass {

    private String nodeDesc;
    private String iterableHashTableDesc;

    public HashTableIterator() {
        super("HashTableIterator", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("head", "#Node", false),
                high("table", "#IterableHashTable", false),
                high("index", "I", false),
                high("tail", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        nodeDesc = Utils.formatAsClass(Matchers.getClass("Node").getObfName());
        iterableHashTableDesc = Utils.formatAsClass(Matchers.getClass("IterableHashTable").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

                !Modifier.isFinal(clazz.access) &&

                SearchUtils.hasInterfaces(clazz, "java/util/Iterator") &&

                SearchUtils.countObjectFields(clazz) == 4 &&

                Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                        n -> !Modifier.isStatic(n.access) && n.desc.equals(nodeDesc),
                        iht -> !Modifier.isStatic(iht.access) && iht.desc.equals(iterableHashTableDesc),
                        i -> !Modifier.isStatic(i.access) && i.desc.equals("I")),2,1,1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode remove = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                m.name.equals("remove") && SearchUtils.isReturnType(m, "V"));
        //If Jagex's obfuscator decides to change its name
        if(remove == null)
            remove = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    Modifier.isPublic(m.access) && SearchUtils.isReturnType(m, "V"));

        if(remove != null) {
            insert("remove", remove);

            MethodSearcher methodSearcher = new MethodSearcher(remove);

            /* head ( #Node ) */
            Pattern head = methodSearcher.singularSearch(f -> ((FieldInsnNode)f).owner.equals(clazz.name),
                    0, Opcodes.GETFIELD);

            if(head.isFound()) {
                insert("head", (FieldInsnNode) head.getFirst());

                /* tail ( #Node ) */
                FieldNode tail = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals(nodeDesc) && !isHookFound(f.name, true));
                if(tail != null)
                    insert("tail", clazz.name, tail.name, tail.desc);

            }

        }

        /* table ( #IterableHashTable ) */
        FieldNode table = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(iterableHashTableDesc));
        if(table != null)
            insert("table", clazz.name, table.name, table.desc);

        /* index ( I ) */
        FieldNode index = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(index != null)
            insert("index", clazz.name, index.name, index.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "IterableHashTable" };
    }

}
