package org.rsminion.classes.impl.scene;

import jdk.internal.org.objectweb.asm.Opcodes;
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
import java.util.List;

@SuppressWarnings("unchecked")
public class Model extends RSClass {

    public Model() {
        super("Model", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                //Important
                high("indicesLength", "I", false),
                high("verticesLength", "I", false),
                high("verticesX", "[I", false),
                high("verticesY", "[I", false),
                high("verticesZ", "[I", false),
                high("indicesX", "[I", false),
                high("indicesY", "[I", false),
                high("indicesZ", "[I", false),

                /*
                high("uidCount", "I", true),
                high("onCursorUIDs", "[J", true),
                high("diameter", "I", false),
                high("radius", "I", false),
                high("singleTile", "Z", false),
                high("boundsType", "I", false),
                high("centerX", "I", false),
                high("centerY", "I", false),
                high("centerZ", "I", false),
                high("vectorSkin", "[[I", false),
                high("vertexGroups", "[[I", false) ^ same
                //The rest is un-necessary for now
                */
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Renderable") &&
            SearchUtils.countObjectFields(clazz) >= 20 &&
            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    i -> !Modifier.isStatic(i.access) && i.desc.equals("I"),
                    ia -> !Modifier.isStatic(ia.access) && ia.desc.equals("[I")),
                    14, 12))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* toSharedModel (Z)Model : indicesLength */
        MethodNode toSharedModel = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, Utils.formatAsClass(clazz.name)) &&
                SearchUtils.containsParams(m, "Z"));
        if(toSharedModel != null) {
            insert("toSharedModel", toSharedModel);
            methodSearcher.setMethod(toSharedModel);

            /* indicesLength ( I ) */
            Pattern indicesLength = methodSearcher.cycleInstances(
                    i -> methodSearcher.linearSearch(0, Opcodes.GETFIELD, Opcodes.BIPUSH),
                    p -> {
                        FieldInsnNode fin = (FieldInsnNode) p.getFirst();
                        IntInsnNode bipush = (IntInsnNode) p.get(1);
                        return fin.owner.equals(clazz.name) && fin.desc.equals("I") && bipush.operand == 100;
                    }, 100);
            //Back-up search
            if(!indicesLength.isFound())
                indicesLength = methodSearcher.singularSearch(a -> {
                    FieldInsnNode fin = (FieldInsnNode) a;
                    return fin.owner.equals(clazz.name) && fin.desc.equals("I");
                }, 0, Opcodes.GETFIELD);

            if(indicesLength.isFound())
                insert("indicesLength", indicesLength.getFirstFieldNode());
        }

        /* scale (III)V : verticesLength, verticesX, verticesY, verticesZ */
        MethodNode scale = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") && !Modifier.isFinal(m.access) &&
                SearchUtils.countParam(m, "I") >= 3 &&
                m.instructions.size() < 200 &&
                methodSearcher.get(m).singularIntSearch(128, 0,
                        Opcodes.SIPUSH).isFound());
        if(scale != null) {
            methodSearcher.setMethod(scale);
            insert("scale", scale);

            /* verticesLength ( I ) */
            Pattern verticesLength = methodSearcher.singularSearch(a -> {
                FieldInsnNode fin = (FieldInsnNode) a;
                return fin.owner.equals(clazz.name) && fin.desc.equals("I");
            }, 0, Opcodes.GETFIELD);
            if(verticesLength.isFound())
                insert("verticesLength", verticesLength.getFirstFieldNode());

            /* vertices ( [I ) */
            Pattern[] vertices = methodSearcher.linearSearchAll(p -> {
                        FieldInsnNode fin = p.getFirstFieldNode();
                        return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
                    }, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD,
                    Opcodes.ILOAD, Opcodes.IMUL);

            if(vertices.length < 3) //Back-up
                vertices = methodSearcher.singularSearchAll(p -> {
                    FieldInsnNode fin = p.getFirstFieldNode();
                    return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
                }, Opcodes.GETFIELD);

            if(vertices.length >= 3) {
                List<Pattern> sortedVertices = Utils.removeEmpty(
                        sortArraysBasedOnVar(methodSearcher, Opcodes.ILOAD, 1, vertices));
                if(sortedVertices.size() >= 3) {
                    /* verticesX ( [I ) */
                    insert("verticesX", sortedVertices.get(0).getFirstFieldNode());
                    /* verticesY ( [I ) */
                    insert("verticesY", sortedVertices.get(1).getFirstFieldNode());
                    /* verticesZ ( [I ) */
                    insert("verticesZ", sortedVertices.get(2).getFirstFieldNode());
                }
            }
        }

        /* draw(I) : indicesX, indicesY, indicesZ */ //a.k.a rasterize(I)
        List<MethodNode> drawMethods = classSearcher.findMethods(m ->
                !Modifier.isStatic(m.access) &&
                !Modifier.isPublic(m.access) &&
                 Modifier.isFinal(m.access) &&
                SearchUtils.isReturnType(m, "V") &&
                SearchUtils.containsParams(m, "I") &&
                SearchUtils.countParam(m, "I") <= 3);
        if(drawMethods.size() > 0) {
            MethodNode draw = SearchUtils.getLeastInstructionLength(drawMethods.
                    toArray(new MethodNode[0]));
            methodSearcher.setMethod(draw);

            /* indices ( [I ) */
            Pattern[] intArrays = methodSearcher.linearSearchAll(
                    p -> {
                        FieldInsnNode fin = p.getFirstFieldNode();
                        return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
                    }, Opcodes.GETFIELD, Opcodes.ILOAD, Opcodes.IALOAD, Opcodes.ISTORE);
            //Back-up search if pattern scrambles ^
            if(intArrays.length < 3)
                intArrays = methodSearcher.singularSearchAll(
                        p -> {
                            FieldInsnNode fin = p.getFirstFieldNode();
                            return fin.owner.equals(clazz.name) &&
                                    fin.desc.equals("[I");}, Opcodes.GETFIELD);

            if(intArrays.length >= 3) {
                List<Pattern> sortedPatterns = Utils.removeEmpty(
                        sortArraysBasedOnVar(methodSearcher, Opcodes.ISTORE, 0, intArrays));
                if(sortedPatterns.size() >= 3) {
                    /* indicesX ( [I ) */
                    insert("indicesX", sortedPatterns.get(0).getFirstFieldNode());
                    /* indicesY ( [I ) */
                    insert("indicesY", sortedPatterns.get(1).getFirstFieldNode());
                    /* indicesZ ( [I ) */
                    insert("indicesZ", sortedPatterns.get(2).getFirstFieldNode());
                }
            }
        }
    }

    private Pattern[] sortArraysBasedOnVar(MethodSearcher methodSearcher, int opcode, int instance, Pattern[] intArrays) {
        Pattern[] result = new Pattern[20];
        for(Pattern pattern : intArrays) {
            AbstractInsnNode var = methodSearcher.getAbstractInsnNode(instance, opcode,
                    pattern.getFirstLine() + 1, 10, true, true);
            if(var != null) {
                int index = ((VarInsnNode) var).var;
                if(index < 20 && result[index] == null)
                    result[index] = pattern;
            }
        }
        return result;
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }

}
