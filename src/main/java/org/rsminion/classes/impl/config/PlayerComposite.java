package org.rsminion.classes.impl.config;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.searchers.MethodSearcher;
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class PlayerComposite extends RSClass {

    public PlayerComposite() {
        super("PlayerComposite", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("npcID", "I", false),
                high("equipmentIDs", "[I", false),
                high("baseHashID", "J", false),
                high("uniqueHashID", "J", false),
                high("bodyColors", "[I", false),
                high("female", "Z", false)
        };
    }

    @Override
    protected boolean locateClass() {
        RSClass player = Matchers.getClass("Player");
        if(player.isHookFound("composite"))
            registerClass(GamePack.get(Utils.stripClassFormat(player.
                    getHook("composite").getDesc())));
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        MethodNode calculateHashID = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") && SearchUtils.getParameters(m).length <= 1
                && methodSearcher.get(m).singularIntSearch(256, 0, Opcodes.SIPUSH).isFound()
        /*Alternate to 256 Int Pattern: && methodSearcher.get(m).singularSearch(f -> ((FieldInsnNode)f).owner.equals(clazz.name) &&
                ((FieldInsnNode)f).desc.equals("[I"), 0, Opcodes.GETFIELD).isFound() &&
                methodSearcher.singularSearch(f -> ((FieldInsnNode)f).owner.equals(clazz.name) &&
                        ((FieldInsnNode)f).desc.equals("J"), 0, Opcodes.GETFIELD).isFound()*/);
        if(calculateHashID != null) {
            insert("calculateHashID", calculateHashID);
            methodSearcher.setMethod(calculateHashID);

            /* equipmentIDs ( [I ) */
            Pattern current = methodSearcher.searchForFrequent(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
            }, Opcodes.GETFIELD);
            if(current.isFound())
                insert("equipmentIDs", current.getFirstFieldNode());

            /* baseHashID ( J ) */
            current = methodSearcher.searchForFrequent(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("J");
            }, Opcodes.GETFIELD);
            if(current.isFound())
                insert("baseHashID", current.getFirstFieldNode());
        }

        /* bodyColors ( [I ) */
        if(isHookFound("equipmentIDs")) {
            FieldNode bodyColors = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals("[I") && !isHookFound(f.name, true));
            if(bodyColors != null)
                insert("bodyColors", clazz.name, bodyColors.name, bodyColors.desc);
        }

        /* uniqueHashID ( J ) */
        if(isHookFound("baseHashID")) {
            FieldNode uniqueHashID = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals("J") && !isHookFound(f.name, true));
            if(uniqueHashID != null)
                insert("uniqueHashID", clazz.name, uniqueHashID.name, uniqueHashID.desc);
        }

        /* female ( Z ) */
        FieldNode female = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Z"));
        if(female != null)
            insert("female", clazz.name, female.name, female.desc);

        /* npcID ( I ) */
        FieldNode npcID = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(npcID != null)
            insert("npcID", clazz.name, npcID.name, npcID.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Player" };
    }
}
