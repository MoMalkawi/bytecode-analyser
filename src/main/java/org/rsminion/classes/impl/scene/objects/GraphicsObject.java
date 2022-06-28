package org.rsminion.classes.impl.scene.objects;

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

//SpotAnimation
@SuppressWarnings("unchecked")
public class GraphicsObject extends RSClass {

    public GraphicsObject() {
        super("GraphicsObject", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "I", false),
                high("height", "I", false),
                high("plane", "I", false),
                high("x", "I", false),
                high("y", "I", false),
                high("startCycle", "I", false),
                high("finished", "Z", false),
                high("currentFrameIndex", "I", false),
                high("currentFrameLength", "I", false)
                //AnimationSequence
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.isParent(clazz, "Renderable") &&
                    Utils.isBetween(Searcher.countFieldNodes(clazz,
                            i -> !Modifier.isStatic(i.access) && i.desc.equals("I"))[0],
                            7, 10))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* finished ( Z ) */
        FieldNode finished = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Z"));
        if(finished != null)
            insert("finished", clazz.name, finished.name, finished.desc);

        //TODO: Add AnimationSequence Later On (FieldNode)

        MethodNode constructor = getConstructor();
        if(constructor != null && isHookFound("finished")) {
            insert("AnimableObject", constructor);

            methodSearcher.setMethod(constructor);

            assert finished != null;

            Pattern current = methodSearcher.searchForKnown(clazz.name, finished.name);
            int finishedLine = current.getFirstLine();

            if(current.isFound())
                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        0, clazz.name);

            /* id ( I ) */
            if(current.isFound()) {
                insert("id", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        1, clazz.name);
            }

            /* plane ( I ) */
            if(current.isFound()) {
                insert("plane", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        1, clazz.name);
            }

            /* x ( I ) */
            if(current.isFound()) {
                insert("x", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        1, clazz.name);
            }

            /* y ( I ) */
            if(current.isFound()) {
                insert("y", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        1, clazz.name);
            }

            /* height ( I ) */
            if(current.isFound()) {
                insert("height", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        1, clazz.name);
            }

            /* startCycle ( I ) */
            if(current.isFound())
                insert("startCycle", current.getFirstFieldNode());

            /* stepFrame (I)V: currentFrameLength, currentFrameIndex */
            MethodNode stepFrame = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    Modifier.isFinal(m.access) && SearchUtils.isReturnType(m, "V") &&
                    SearchUtils.containsParams(m, "I") && SearchUtils.getParameters(m).length <= 3);
            if(stepFrame != null) {
                insert("stepFrame", stepFrame);

                methodSearcher.setMethod(stepFrame);

                current = methodSearcher.jumpSearch(p -> {
                            FieldInsnNode fin = p.getFirstFieldNode();
                            return fin.owner.equals(clazz.name) && fin.desc.equals("I");
                        }, Opcodes.GOTO, finishedLine,
                        100, 0, Opcodes.GETFIELD);

                /* currentFrameLength ( I ) */
                if(current.isFound()) {
                    insert("currentFrameLength", current.getFirstFieldNode());

                    int frameLengthLine = current.getFirstLine();
                    current = methodSearcher.cycleInstances(
                            f -> methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                                    "I", frameLengthLine, f, clazz.name),
                            p -> !isHookFound(p.getFirstFieldNode().name, true),
                            100);
                }

                /* currentFrameIndex ( I ) */
                if(current.isFound())
                    insert("currentFrameIndex", current.getFirstFieldNode());

            }
        }

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }

}
