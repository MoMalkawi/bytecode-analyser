package org.rsminion.classes.impl.scene.objects;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
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
public class AnimableObject extends RSClass {

    public AnimableObject() {
        super("AnimableObject", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "I", false),
                high("clickType", "I", false),
                high("orientation", "I", false),
                high("plane", "I", false),
                high("x", "I", false),
                high("y", "I", false),
                high("animationFrame", "I", false),
                high("animationDelay", "I", false)
                //Sequence...
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(!Modifier.isFinal(clazz.access) &&
                    SearchUtils.isParent(clazz, "Renderable") &&
                    Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                            i -> !Modifier.isStatic(i.access) &&
                                    i.desc.equals("I"),
                            s -> !Modifier.isStatic(s.access) &&
                                    s.desc.startsWith("L")), 8, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        MethodSearcher methodSearcher = new MethodSearcher();

        MethodNode constructor = getConstructor();
        if(constructor != null) {
            insert("AnimableObject", constructor);
            methodSearcher.setMethod(constructor);

            /* Find Starting Point ( id ) */
            Pattern current = methodSearcher.singularPatternSearch(f -> {
                FieldInsnNode fin = f.getFirstFieldNode();
                if(fin.desc.equals("I") && fin.owner.equals(clazz.name)) {
                    Pattern iload = methodSearcher.singularSearch(i ->
                            ((VarInsnNode)i).var == 1, f.getFirstLine() - 6,
                            f.getFirstLine(), 0, Opcodes.ILOAD);
                    return iload.isFound();
                }
                return false;
            }, 0, constructor.instructions.size(),
                    0, Opcodes.PUTFIELD);

            /* Back-up */
            if(!current.isFound())
                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        0, 0, clazz.name);

            /* id ( I ) */
            if(current.isFound()) {
                insert("id", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* clickType ( I ) */
            if(current.isFound()) {
                insert("clickType", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* orientation ( I ) */
            if(current.isFound()) {
                insert("orientation", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* plane ( I ) */
            if(current.isFound()) {
                insert("plane", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* x ( I ) */
            if(current.isFound()) {
                insert("x", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* y ( I ) */
            if(current.isFound()) {
                insert("y", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 2, clazz.name); //Change to 1 when Sequence is added
            }

            /* sequence ( sequence ) */
            //TODO: add later after #Sequence is added

            /* animationFrame ( I ) */
            if(current.isFound()) {
                insert("animationFrame", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* animationDelay ( I ) */
            if(current.isFound())
                insert("animationDelay", current.getFirstFieldNode());

        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }

}
