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
public class Cache extends RSClass {

    private String cacheNodeDesc;

    public Cache() {
        super("Cache", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("table", "#IterableHashTable", false),
                high("queue", "#Queue", false),
                high("cacheNode", "#CacheNode", false),
                high("remaining", "I", false),
                high("size", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        cacheNodeDesc = Utils.formatAsClass(Matchers.getClass("CacheNode").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

            SearchUtils.isPublicFinal(clazz.access) &&

            SearchUtils.countObjectFields(clazz) == 5 &&

            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    i -> !Modifier.isStatic(i.access) && i.desc.equals("I"),
                    cn -> !Modifier.isStatic(cn.access) && cn.desc.equals(cacheNodeDesc)),
                    2,1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode remove = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                m.desc.equals("(J)V"));

        //Just in case the Obfuscator plays with the long param. (sometimes it changes to Long then casts)
        if(remove == null)
            remove = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    SearchUtils.getParameters(m).length == 1 && SearchUtils.isReturnType(m, "V"));

        if(remove != null) {
            insert("remove", remove);

            MethodSearcher methodSearcher = new MethodSearcher(remove);

            Pattern remaining = methodSearcher.linearSearch(0, Opcodes.ALOAD,
                    Opcodes.DUP, Opcodes.GETFIELD);

            //Just in case
            if(!remaining.isFound())
                remaining = methodSearcher.singularSearch(f -> ((FieldInsnNode)f).owner.equals(clazz.name),
                        0, Opcodes.GETFIELD);

            if(remaining.isFound()) {

                /* remaining ( I ) */
                insert("remaining", remaining.getFieldNodes().get(0));

                /* size ( I ) */
                FieldNode size = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("I") && !isHookFound(f.name, true));
                if(size != null)
                    insert("size", clazz.name, size.name, size.desc);

            }

        }

        /* table ( #IterableHashTable ) */
        FieldNode table = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("IterableHashTable").getObfName())));
        if(table != null)
            insert("table", clazz.name, table.name, table.desc);

        /* cacheNode ( #CacheNode ) */
        FieldNode cacheNode = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(cacheNodeDesc));
        if(cacheNode != null)
            insert("cacheNode", clazz.name, cacheNode.name, cacheNode.desc);

        /* queue ( #Queue ) */
        FieldNode queue = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("Queue").getObfName())));
        if(queue != null)
            insert("queue", clazz.name, queue.name, queue.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "IterableHashTable", "Queue", "CacheNode" };
    }

}
