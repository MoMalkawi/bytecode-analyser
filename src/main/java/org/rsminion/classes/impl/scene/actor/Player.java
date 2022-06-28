package org.rsminion.classes.impl.scene.actor;

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
public class Player extends RSClass {

    public Player() {
        super("Player", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("composite", "#PlayerComposite", false),
                high("standingStill", "Z", false),
                high("skullIcon", "I", false),
                high("overheadIcon", "I", false),
                high("team", "I", false),
                high("hidden", "Z", false),
                high("level", "I", false),
                high("totalLevel", "I", false),
                high("name", "#NameComposite", false),
                high("model", "#Model", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Actor") &&
            SearchUtils.hasFieldType(clazz, "[Ljava/lang/String;"))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* name ( #NameComposite ) */
        if(Matchers.isFound("NameComposite")) {
            FieldNode name = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals(Utils.formatAsClass(Matchers.getObfClass("NameComposite"))));
            insert("name", clazz.name, name.name, name.desc);
        }

        /* model ( #Model ) */
        if(Matchers.isFound("Model")) {
            FieldNode model = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals(Utils.formatAsClass(Matchers.getObfClass("Model"))));
            insert("model", clazz.name, model.name, model.desc);
        }

        /* composite ( #PlayerComposite ) */
        FieldNode composite = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                !isHookFound(f.name, true) && SearchUtils.isClass(f.desc) &&
                Utils.checkIntArrayMatch(Searcher.countFieldNodes(GamePack.get(Utils.stripClassFormat(f.desc)),
                        ia -> !Modifier.isStatic(ia.access) && ia.desc.equals("[I"),
                        l -> !Modifier.isStatic(l.access) && l.desc.equals("J")), 2, 2));
        if(composite != null)
            insert("composite", clazz.name, composite.name, composite.desc);


        MethodNode decode = classSearcher.findMethod(f -> !Modifier.isStatic(f.access) &&
                SearchUtils.isReturnType(f, "V") && SearchUtils.containsParams(f,
                Utils.formatAsClass(Matchers.getObfClass("Buffer"))));
        if(decode != null) {
            insert("decode", decode);
            methodSearcher.setMethod(decode);

            Pattern current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                    0, 0, clazz.name);

            /* skullIcon ( I ) */
            if(current.isFound()) {
                insert("skullIcon", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* overheadIcon ( I ) */
            if(current.isFound()) {
                insert("overheadIcon", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* team ( I ) */
            if(current.isFound())
                insert("team", current.getFirstFieldNode());

            /* hidden ( Z ) */
            current = methodSearcher.singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("Z");
            }, 0, Opcodes.PUTFIELD);
            if(current.isFound())
                insert("hidden", current.getFirstFieldNode());

            if(isHookFound("name")) {
                RSHook name = getHook("name");
                current = methodSearcher.searchForKnown(name.getObfOwner(),
                        name.getObfName());
                if(current.isFound())
                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 0, clazz.name);

                /* level ( I ) */
                if(current.isFound()) {
                    insert("level", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);
                }

                /* totalLevel ( I ) */
                if(current.isFound())
                    insert("totalLevel", current.getFirstFieldNode());

                //Note: Next PUTFIELD is ( hidden ( Z ) ) <-- alternate pattern
            }

        }

        MethodNode getModel = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, Utils.formatAsClass(Matchers.
                        getObfClass("Model"))) &&
                SearchUtils.getParameters(m).length <= 1 &&
                methodSearcher.get(m).singularIntSearch(1536, 0,
                        Opcodes.SIPUSH).isFound());

        if(getModel != null) {
            insert("getModel", getModel);
            methodSearcher.setMethod(getModel);

            Pattern standingStill = methodSearcher.singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("Z");
            }, 0, Opcodes.GETFIELD);

            /* standingStill ( Z ) */
            if(standingStill.isFound())
                insert("standingStill", standingStill.getFirstFieldNode());
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Actor" };
    }

}
