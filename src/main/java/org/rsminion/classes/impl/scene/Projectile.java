package org.rsminion.classes.impl.scene;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
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

public class Projectile extends RSClass {
    
    public Projectile() {
        super("Projectile", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "I", false),
                high("plane", "I", false),
                high("sourceX", "I", false),
                high("sourceY", "I", false),
                high("height", "I", false),
                high("startHeight", "I", false),
                high("endHeight", "I", false),
                high("cycleStart", "I", false),
                high("cycleEnd", "I", false),
                high("slope", "I", false),
                high("targetIndex", "I", false),
                high("isMoving", "Z", false),
                high("x", "D", false),
                high("y", "D", false),
                high("z", "D", false),
                high("speedX", "D", false),
                high("speedY", "D", false),
                high("speedZ", "D", false),
                //high("sequenceDefinition", "#SequenceDefinition", false), //If needed later
                high("rotationX", "I", false),
                high("rotationY", "I", false),
                high("heightOffset", "D", false),
                high("scalar", "D", false),
                high("frameProgress", "I", false),
                high("currentFrameIndex", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.isParent(clazz, "Renderable") &&
            Utils.isBetween(SearchUtils.countObjectFields(clazz), 20, 30))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* isMoving ( Z ) */
        FieldNode isMoving = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Z"));
        if(isMoving != null)
            insert("isMoving", clazz.name, isMoving.name, isMoving.desc);

        /* update ( I ) */
        MethodNode update = classSearcher.findMethod(m ->
            !Modifier.isStatic(m.access) &&
            Modifier.isFinal(m.access) && SearchUtils.isReturnType(m,"V") &&
            Utils.isBetween(SearchUtils.countParam(m, "I"), 0, 3));
        if(update != null && isHookFound("isMoving")) {
            insert("update", update);
            methodSearcher.setMethod(update);

            assert isMoving != null;

            //Fetching Start Line
            Pattern current = methodSearcher.searchForKnown(clazz.name, isMoving.name);
            if(current.isFound())
                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        0, clazz.name);

            /* x ( D ) */
            if(current.isFound()) {
                insert("x", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        1, clazz.name);
            }

            /* speedX ( D ) */
            if(current.isFound()) {
                insert("speedX", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        1, clazz.name);
            }

            /* y ( D ) */
            if(current.isFound()) {
                insert("y", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        1, clazz.name);
            }

            /* speedY ( D ) */
            if(current.isFound()) {
                insert("speedY", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        1, clazz.name);
            }

            /* z ( D ) */
            if(current.isFound()) {
                insert("z", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        1, clazz.name);
            }

            /* speedZ ( D ) */
            if(current.isFound()) {
                insert("speedZ", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        1, clazz.name);
            }

            /* heightOffset ( D ) */
            if(current.isFound()) {
                insert("heightOffset", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        0, clazz.name);
            }

            /* rotationX ( I ) */
            if(current.isFound()) {
                insert("rotationX", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
                        2, clazz.name);
            }

            /* scalar ( D ) */
            if(current.isFound()) {
                insert("scalar", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        0, clazz.name);
            }

            /* rotationY ( I ) */
            if(current.isFound()) {
                insert("rotationY", current.getFirstFieldNode());

                //current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "AnimationSequence", current.getFirstLine(),
                //        0);
                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "I", current.getFirstLine(),
                        0, clazz.name);
            }

            /* animationSequence ( #AnimationSequence ) */
            //TODO: insert here after analyzing AnimationSequence

            /* frameProgress ( I ) */
            if(current.isFound()) {
                insert("frameProgress", current.getFirstFieldNode());

                /* currentFrameIndex Search */
                int line = current.getFirstLine();
                current = methodSearcher.cycleInstances(
                        f -> methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                                "I", line, f, clazz.name),
                        p -> !isHookFound(p.getFirstFieldNode().name, true),
                        100);
            }

            /* currentFrameIndex ( I ) */
            if(current.isFound())
                insert("currentFrameIndex", current.getFirstFieldNode());

        }
        //End of update(I)

        MethodNode constructor = getConstructor();
        if(constructor != null) {
            insert("Projectile", constructor);
            methodSearcher.setMethod(constructor);

            Pattern current;
            int instance = 0;
            int loops = 0;
            while((current = methodSearcher.linearSearch(instance,
                    Opcodes.ALOAD, Opcodes.ILOAD)).isFound()) {
                if(loops > 200) break;
                String fieldName = null;
                /* All Field Below ( I ) */
                switch (((VarInsnNode) current.get(1)).var) {
                    case 1:
                        fieldName = "id";
                        break;
                    case 2:
                        fieldName = "plane";
                        break;
                    case 3:
                        fieldName = "sourceX";
                        break;
                    case 4:
                        fieldName = "sourceY";
                        break;
                    case 5:
                        fieldName = "height";
                        break;
                    case 6:
                        fieldName = "cycleStart";
                        break;
                    case 7:
                        fieldName = "cycleEnd";
                        break;
                    case 8:
                        fieldName = "slope";
                        break;
                    case 9:
                        fieldName = "startHeight";
                        break;
                    case 10:
                        fieldName = "targetIndex"; //a.k.a interactingID, targetID
                        break;
                    case 11:
                        fieldName = "endHeight";
                        break;
                }
                if(fieldName != null) {
                    Pattern putField = methodSearcher.singularSearch(current.getFirstLine(),
                            current.getFirstLine() + 15, 0, Opcodes.PUTFIELD);
                    if(putField.isFound())
                        insert(fieldName, putField.getFirstFieldNode());
                }
                instance++;
                loops++;
            }
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }

}
/*
                Pattern current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", moving.getFirstLine(),
                        0);

                if(current.isFound()) {

insert("x", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", moving.getFirstLine(),
        2);
        if(current.isFound()) {
        insert("speedX", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
        1);
        }
        }

        if(current.isFound()) {

        insert("y", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", moving.getFirstLine(),
        0);
        if(current.isFound()) {
        insert("speedY", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
        1);
        }

        }

        if(current.isFound()) {

        insert("z", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
        1);

        }

        if(current.isFound()) {

        insert("speedZ", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", moving.getFirstLine(),
        1);
        if(current.isFound()) {
        insert("heightOffset", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
        0);
        }

        }

        if(current.isFound()) {
        insert("rotationX", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", current.getFirstLine(),
        0);
        }

        if(current.isFound()) {

        //If it's speedZ, then the next GetField is scalar
        if(isHookFound(current.getFirstFieldNode().name, true))
        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "D", moving.getFirstLine(),
        1);

        if(current.isFound())
        insert("scalar", current.getFirstFieldNode());

        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", moving.getFirstLine(),
        0);
        if(current.isFound())
        insert("rotationY", current.getFirstFieldNode());

                    // Uncomment when AnimationSequence is done. TODO
                    current = methodSearcher.singularSearch(f -> {
                                FieldInsnNode fin = (FieldInsnNode) f;
                                return fin.owner.equals(clazz.name) && fin.desc.equals(Utils.formatAsClass(Matchers.
                                        getClass("AnimationSequence").getObfName()));
                            }, current.getFirstLine(),current.getFirstLine() + 20, 0,
                            Opcodes.GETFIELD);
                    if(current.isFound())
                        insert("animationSequence", current.getFirstFieldNode());


        if(current.isFound()) {
        int finalSectionIndex = current.getFirstLine();

        current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "I", current.getFirstLine(),
        1);
        if(current.isFound())
        insert("frameProgress", current.getFirstFieldNode());

        Pattern ifJump = methodSearcher.singularSearch(finalSectionIndex, finalSectionIndex + 50,
        0, Pattern.IF_WILDCARD);
        if(ifJump.isFound()) {
        current = methodSearcher.jumpSearch(p -> {
        FieldInsnNode fin = p.getFirstFieldNode();
        return fin.owner.equals(clazz.name) && fin.desc.equals("I");
        }, ifJump.getFirst().getOpcode(), ifJump.getFirstLine(),
        100, 0, Opcodes.PUTFIELD);
        if(current.isFound())
        insert("currentFrameIndex", current.getFirstFieldNode());
        }
        }
        }
        */