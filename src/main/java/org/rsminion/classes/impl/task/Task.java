package org.rsminion.classes.impl.task;

import org.objectweb.asm.Opcodes;
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
public class Task extends RSClass {

    public Task() {
        super("Task", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("status", "I", false),
                low("intOperand", "I", false),
                low("result", "Ljava/lang/Object;", false),
                low("type", "I", false),
                low("task", "#Task", false),
                low("objOperand", "Ljava/lang/Object;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            !SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.countObjectFields(clazz) == 6 &&
            SearchUtils.countObjectMethods(clazz) == 1 &&
            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    i -> !Modifier.isStatic(i.access) && i.desc.equals("I"),
                    ob -> !Modifier.isStatic(ob.access) && SearchUtils.isNativeObject(ob.desc),
                    t -> !Modifier.isStatic(t.access) && t.desc.equals(Utils.formatAsClass(clazz.name))),
                    3, 2, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode constructor = getConstructor();
        if(constructor != null) {
            insert("Task", constructor);

            MethodSearcher methodSearcher = new MethodSearcher(constructor);

            /* status ( I ) */
            Pattern status = methodSearcher.singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("I");
            }, 0, Opcodes.PUTFIELD);

            if(status.isFound()) {
                insert("status", (FieldInsnNode) status.getFirst());

                /* intOperand ( I ) */
                FieldNode intOperand = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("I") && Modifier.isPublic(f.access) && !isHookFound(f.name, true));
                if(intOperand != null)
                    insert("intOperand", clazz.name, intOperand.name, intOperand.desc);

                /* type ( I ) */
                FieldNode type = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("I") && !Modifier.isPublic(f.access) && !isHookFound(f.name, true));
                if(type != null)
                    insert("type", clazz.name, type.name, type.desc);
            }
        }

        /* result ( Ljava/lang/Object; ) */
        FieldNode result = classSearcher.findField(f -> !Modifier.isStatic(f.access)
                && SearchUtils.isNativeObject(f.desc) && Modifier.isPublic(f.access));
        if(result != null) {
            insert("result", clazz.name, result.name, result.desc);

            /* objOperand ( Ljava/lang/Object; ) */
            FieldNode objOperand = classSearcher.findField(f -> !Modifier.isStatic(f.access)
                    && SearchUtils.isNativeObject(f.desc) && !Modifier.isPublic(f.access) &&
                    !isHookFound(f.name, true));
            if(objOperand != null)
                insert("objOperand", clazz.name, objOperand.name, objOperand.desc);
        }

        /* task ( #Task ) */
        FieldNode task = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(clazz.name)));
        if(task != null)
            insert("task", clazz.name, task.name, task.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }

}
