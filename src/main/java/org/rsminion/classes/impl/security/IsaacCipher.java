package org.rsminion.classes.impl.security;

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
public class IsaacCipher extends RSClass {

    public IsaacCipher() {
        super("IsaacCipher", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("valuesRemaining", "I", false),
                low("randResult", "[I", false),
                low("mm", "[I", false)
                //There are 3 more un-identified fields, but not needed right now.
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
                SearchUtils.isPublicFinal(clazz.access) &&
                SearchUtils.countObjectFields(clazz) == 6 &&
                Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                        ia -> !Modifier.isStatic(ia.access) && ia.desc.equals("[I"),
                        i -> !Modifier.isStatic(i.access) && i.desc.equals("I")), 2, 4))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode nextInt = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isPublicFinal(m.access) && SearchUtils.isReturnType(m, "I") &&
                SearchUtils.containsAnyOpcodes(m, Opcodes.IFNE, Opcodes.IFEQ));
        if(nextInt != null) {
            insert("nextInt", nextInt);

            MethodSearcher methodSearcher = new MethodSearcher(nextInt);

            /* ValuesRemaining ( I ) */
            Pattern valuesRemaining = methodSearcher.singularSearch(o ->  {
                FieldInsnNode fin = (FieldInsnNode) o;
                return fin.owner.equals(clazz.name) && fin.desc.equals("I");
            }, 0, nextInt.instructions.size(), 0, Opcodes.GETFIELD);
            if(valuesRemaining.isFound())
                insert("valuesRemaining", (FieldInsnNode) valuesRemaining.getFirst());

            /* randResult ( [I ) */
            Pattern randResult = methodSearcher.singularSearch(o ->  {
                FieldInsnNode fin = (FieldInsnNode) o;
                return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
            }, 0, nextInt.instructions.size(), 0, Opcodes.GETFIELD);
            if(randResult.isFound()) {
                insert("randResult", (FieldInsnNode) randResult.getFirst());

                /* mm ( [I ) */
                FieldNode mm = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("[I") && !isHookFound(f.name, true));
                if(mm != null)
                    insert("mm", clazz.name, mm.name, mm.desc);
            }

        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }

}
