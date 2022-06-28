package org.rsminion.classes.impl.config;

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
import org.rsminion.tools.utils.Filter;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;
import java.util.List;

@SuppressWarnings("unchecked")
public class NpcComposite extends RSClass {

    public NpcComposite() {
        super("NpcComposite", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("name", "Ljava/lang/String;", false),
                high("id", "I", false),
                high("level", "I", false),
                high("actions", "[Ljava/lang/String;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(!Modifier.isFinal(clazz.access) &&
                    SearchUtils.isParent(clazz, "CacheNode") &&
                    SearchUtils.countObjectFields(clazz) > 20 &&
                    Matchers.getClass("NPC").containsHookType(Utils.
                            formatAsClass(clazz.name)) &&
                    Searcher.countFieldNodes(clazz,
                            i -> !Modifier.isStatic(i.access) &&
                                    i.desc.equals("I"))[0] > 10)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* name ( Ljava/lang/String; ) */
        FieldNode name = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/lang/String;"));
        if(name != null)
            insert("name", clazz.name, name.name, name.desc);

        /* actions ( [Ljava/lang/String; ) */
        FieldNode actions = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[Ljava/lang/String;"));
        if(actions != null)
            insert("actions", clazz.name, actions.name, actions.desc);

        MethodNode getAnimatedModel = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, Utils.formatAsClass(Matchers.getObfClass("Model"))));
        if(getAnimatedModel != null) {
            insert("getAnimatedModel", getAnimatedModel);
            methodSearcher.setMethod(getAnimatedModel);

            Filter<Pattern> idFilter = pattern -> {
                List<FieldInsnNode> fins = pattern.getFieldNodes();
                return fins.get(0).desc.equals(Utils.formatAsClass(Matchers.getObfClass("Cache"))) &&
                        fins.get(1).desc.equals("I");
            };

            /* id ( I ) */
            Pattern id = methodSearcher.cycleInstances(
                    i -> methodSearcher.linearSearch(i,
                            Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.GETFIELD),
                    idFilter,100
            );

            if(!id.isFound())
                id = methodSearcher.cycleInstances(
                        i -> methodSearcher.linearSearch(i,
                                Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.GETFIELD),
                        idFilter,100
                );

            if(id.isFound())
                insert("id", id.getFieldNodes().get(1));

        }

        if(Matchers.isFound("Buffer")) {
            List<MethodNode> methodCandidates = classSearcher.findMethods(m -> !Modifier.isStatic(m.access) &&
                    SearchUtils.isReturnType(m, "V") && SearchUtils.countParam(m, Utils.formatAsClass(Matchers.
                    getObfClass("Buffer"))) == 1);

            MethodNode readNextBuffer = SearchUtils.getMostInstructionLength(
                    methodCandidates.toArray(new MethodNode[0]));
            if(readNextBuffer != null) {
                insert("readNextBuffer", readNextBuffer);
                methodSearcher.setMethod(readNextBuffer);

                //Note: only applies if IF_ICMPNE after int, otherwise jump to field
                Pattern current = methodSearcher.singularIntSearch(95, 0, Opcodes.BIPUSH);
                if(current.isFound())
                    current = methodSearcher.singularSearch(f -> {
                        FieldInsnNode fin = (FieldInsnNode) f;
                        return fin.owner.equals(clazz.name) && fin.desc.equals("I");
                    }, current.getFirstLine(), readNextBuffer.instructions.size(), 0, Opcodes.PUTFIELD);

                /* level ( I ) */
                if(current.isFound())
                    insert("level", current.getFirstFieldNode());
            }
        }

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "NPC", "CacheNode" };
    }

}
