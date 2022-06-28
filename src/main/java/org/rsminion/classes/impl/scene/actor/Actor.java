package org.rsminion.classes.impl.scene.actor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Actor extends RSClass {

    public Actor() {
        super("Actor", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("standAnimation", "I", false), //
                high("overHeadMessage", "Ljava/lang/String;", false),//
                high("hitsplatTypes", "[I", false), //
                high("hitsplatTypes2", "[I", false), //
                high("hitsplatDamages", "[I", false), //
                high("hitsplatValues2", "[I", false), //
                high("hitsplatCycles", "[I", false), //
                high("healthBars", "#Iterable", false), //
                high("interactingID", "I", false), //
                high("runtimeAnimation", "I", false), //
                //high("frameTwo", "I", false),
                high("animation", "I", false),//
                //high("frameOne", "I", false),
                high("animationDelay", "I", false),//
                //high("queueSize", "I", false),
                //high("queueX", "[I", false),
                //high("queueY", "[I", false),
                //high("queueTraversed", "[B", false),
                high("localX", "I", false), //
                high("localY", "I", false), //
                high("orientation", "I", false),//
                high("combatTime", "I", false) //
                //There are other fields, add what I need
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(Modifier.isAbstract(clazz.access) &&
                    SearchUtils.isParent(clazz, "Renderable") &&
                            SearchUtils.countObjectFields(clazz) >= 20)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* overHeadMessage ( Ljava/lang/String; ) */
        FieldNode overHeadMessage = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/lang/String;"));
        if(overHeadMessage != null)
            insert("overHeadMessage", clazz.name, overHeadMessage.name, overHeadMessage.desc);

        /* healthBars ( #Iterable ) */
        FieldNode healthBars = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getObfClass("Iterable"))));
        if(healthBars != null)
            insert("healthBars", clazz.name, healthBars.name, healthBars.desc);

        /* hitSplats (Can be fetched from <init> too by jumpSearches) */
        MethodNode addHitsplat = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") &&
                SearchUtils.containsParams(m, "I") &&
                Utils.isBetween(SearchUtils.countParam(m, "I"), 5, 8));
        if(addHitsplat != null) {
            insert("addHitsplat", addHitsplat);
            methodSearcher.setMethod(addHitsplat);

            Pattern[] hitsplats = methodSearcher.linearSearchAll(f -> {
                FieldInsnNode fin = f.getFirstFieldNode();
                return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
            }, Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.ILOAD);

            /* hitsplats ( [I each ) */
            if(hitsplats != null && hitsplats.length >= 5) {
                Arrays.sort(hitsplats, Comparator.comparingInt(p ->
                        ((VarInsnNode)p.get(2)).var + ((VarInsnNode)p.get(3)).var));
                insert("hitsplatTypes", hitsplats[0].getFirstFieldNode());
                insert("hitsplatDamages", hitsplats[1].getFirstFieldNode());
                insert("hitsplatTypes2", hitsplats[2].getFirstFieldNode());
                insert("hitsplatValues2", hitsplats[3].getFirstFieldNode());
                insert("hitsplatCycles", hitsplats[4].getFirstFieldNode());
            }
        }

        /* localX, localY, orientation */
        List<MethodNode> methodCandidates = Searcher.deepFindMethods(m ->
                Modifier.isStatic(m.access) && Modifier.isFinal(m.access) &&
                m.instructions.getLast().getOpcode() == Opcodes.ATHROW &&
                SearchUtils.containsParams(m, Utils.formatAsClass(clazz.name)) &&
                SearchUtils.getParameters(m).length <= 2); //Account for extra "I" (that's why it's 2)

        MethodNode method = SearchUtils.getMostInstructionLength(methodCandidates.
                toArray(new MethodNode[0]));
        if(method != null) {
            methodSearcher.setMethod(method);

            /* Find Starting Point for localX, localY */
            Pattern current = methodSearcher.jumpSearch(p -> {
                FieldInsnNode fin = p.getFirstFieldNode();
                if(fin.owner.equals(clazz.name) && fin.desc.equals("I")) {
                    Pattern sipush = methodSearcher.singularSearch(p.getFirstLine(),
                            p.getFirstLine() + 6, 0, Opcodes.SIPUSH);
                    return sipush.isFound();
                }
                return false;
            }, Opcodes.GOTO, 0, 200, 0, Opcodes.GETFIELD);

            /* Backup */
            if(!current.isFound())
                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "I",
                        0, 1, clazz.name);

            /* localX ( I ) */
            if(current.isFound()) {
                insert("localX", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "I",
                        current.getFirstLine(), 1, clazz.name);
            }

            /* localY ( I ) */
            if(current.isFound())
                insert("localY", current.getFirstFieldNode());

            Pattern orientation = methodSearcher.searchForFrequent(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("I");
            }, Opcodes.PUTFIELD);

            /* Backup for orientation ( I ) */
            if(!orientation.isFound()) {
                current = methodSearcher.linearSearch(0,
                        Opcodes.ICONST_M1, Opcodes.IF_ICMPEQ);
                if(current.isFound())
                    orientation = methodSearcher.searchLocalJump(Opcodes.PUTFIELD,Opcodes.IF_ICMPEQ,
                            "I", current.getFirstLine(), 0, clazz.name);
            }

            /* orientation ( I ) */
            if(orientation.isFound())
                insert("orientation", orientation.getFirstFieldNode());

            /* animation ( I ) */
            current = methodSearcher.singularIntSearch(13184, 0, Opcodes.SIPUSH);
            if(current.isFound()) {
                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                        0, clazz.name);
                insert("animation", current.getFirstFieldNode());
            }

            /* interactingID ( I ) */
            current = methodSearcher.cycleInstances(
                    i -> methodSearcher.linearSearch(i, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                            Opcodes.LDC),
                    p -> ((FieldInsnNode)p.getFirst()).owner.equals(clazz.name) &&
                                ((LdcInsnNode)p.get(3)).cst.equals(32768),
                    100);
            if(current.isFound())
                insert("interactingID", current.getFirstFieldNode());

            int methodSize = method.instructions.size();

            /* animations */
            current = methodSearcher.linearSearch(c -> {
                        FieldInsnNode fin = c.getFirstFieldNode();
                        if(fin.owner.equals(clazz.name) && fin.desc.equals("Z")) {
                            Pattern next = methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                                    "I", c.getFirstLine(), 0, clazz.name);
                            if(next.isFound())
                                return !isHookFound(next.getFirstFieldNode().name, true);
                        }
                        return false;
                    }, 0, methodSize,
                            0, Opcodes.GETFIELD, Opcodes.IFNE);
            if(current.isFound())
                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                        "I", current.getFirstLine(), 0, clazz.name);

            /* standAnimation ( I ) */
            if(current.isFound()) {
                insert("standAnimation", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                        "I", current.getFirstLine(), 1, clazz.name);
            }

            /* runtimeAnimation ( I ) */
            if(current.isFound())
                insert("runtimeAnimation", current.getFirstFieldNode());

            /* animationDelay ( I ) */
            if(isHookFound("animation")) {

                current = methodSearcher.cycleInstances(
                        f -> methodSearcher.linearSearch(f,
                                Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL,
                                Opcodes.ICONST_M1),
                        p -> {
                            FieldInsnNode fin = p.getFirstFieldNode();
                            RSHook animation = getHook("animation");
                            return fin.owner.equals(animation.getObfOwner()) &&
                                    fin.name.equals(animation.getObfName());
                        }, 100);
                if(current.isFound())
                    current = methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                            "I", current.getFirstLine(), 1, clazz.name);
                /* animationDelay ( I ) */
                if(current.isFound())
                    insert("animationDelay", current.getFirstFieldNode());
            }

        }

        /* combatTime ( I ) */
        method = Searcher.deepFindMethod(m -> Modifier.isStatic(m.access) &&
                Modifier.isFinal(m.access) &&
                SearchUtils.containsParams(m, Utils.formatAsClass(clazz.name), "I") &&
                Utils.isBetween(SearchUtils.countParam(m, "I"), 3,6));
        if(method != null) {
            methodSearcher.setMethod(method);

            Pattern result = methodSearcher.linearMultiSearch(0,
                    new int[][]{
                            {
                                Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL, Opcodes.GETSTATIC,
                                Opcodes.LDC, Opcodes.IMUL
                            },
                            {
                                Opcodes.GETSTATIC, Opcodes.LDC, Opcodes.IMUL, Opcodes.ALOAD,
                                Opcodes.GETFIELD, Opcodes.LDC, Opcodes.IMUL
                            }
                    }
            );
            /* combatTime ( I ) */
            if(result.isFound())
                insert("combatTime", (FieldInsnNode) result.
                        get(f -> f.getOpcode() == Opcodes.GETFIELD));

        }

        //Searcher.findLocations("co", "bu");
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }
}
