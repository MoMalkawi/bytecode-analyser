package org.rsminion.classes.impl.scene.item;

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
import org.rsminion.tools.searchers.data.Pattern;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;
import java.util.List;

public class Item extends RSClass {

    public Item() {
        super("Item", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "I", false),
                high("quantity", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.isParent(clazz, "Renderable") &&
            SearchUtils.countObjectFields(clazz) <= 4)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        List<MethodNode> methods = classSearcher.findMethods(m -> !Modifier.isStatic(m.access) &&
                Modifier.isProtected(m.access) && SearchUtils.isReturnType(m, Utils.
                formatAsClass(Matchers.getClass("Model").getObfName())));
        MethodNode getModel = SearchUtils.getLeastInstructionLength(methods.toArray(new MethodNode[0]));

        if(getModel != null) {
            methodSearcher.setMethod(getModel);

            Pattern id = methodSearcher.linearSearch(p -> p.getFirstFieldNode().owner.equals(clazz.name),
                    0,getModel.instructions.size(),0,
                    Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.INVOKESTATIC);
            if(!id.isFound()) //Back-up
                id = methodSearcher.singularSearch(f -> {
                    FieldInsnNode fin = (FieldInsnNode) f;
                    return fin.owner.equals(clazz.name) && fin.desc.equals("I");
                }, 0, Opcodes.GETFIELD);

            /* id ( I ) */
            if(id.isFound()) {
                insert("id", id.getFirstFieldNode());

                /* quantity ( I ) */
                FieldNode quantity = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("I") && !isHookFound(f.name, true));
                if(quantity != null)
                    insert("quantity", clazz.name, quantity.name, quantity.desc);
            }
        }

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable", "Model" };
    }
}
