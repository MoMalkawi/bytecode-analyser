package org.rsminion.classes.impl.config;

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

public class ObjectComposite extends RSClass {

    private final MethodSearcher methodSearcher = new MethodSearcher();

    public ObjectComposite() {
        super("ObjectComposite", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("name", "Ljava/lang/String;", false),
                high("actions", "[Ljava/lang/String;", false),
                high("modelIDs", "[I", false),
                high("childrenIDs", "[I", false),
                high("width", "I", false),
                high("height", "I", false),
                high("varpBitID", "I", false),
                high("configID", "I", false),
                high("id", "I", false),
                high("modelTypes", "[I", false),
                high("modelSizeX", "I", false),
                high("modelSizeY", "I", false),
                high("modelSizeZ", "I", false),
                high("animationID", "I", false),
                high("translateX", "I", false), //a.k.a offset
                high("translateY", "I", false),
                high("translateZ", "I", false),
                high("unwalkable", "Z", false),
                high("solid", "Z", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "CacheNode") &&
                    SearchUtils.countObjectFields(clazz) >= 11 &&
            Searcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    SearchUtils.isReturnType(m, "V") &&
                    SearchUtils.containsParams(m, Utils.formatAsClass(Matchers.
                            getObfClass("Buffer"))) &&
                    methodSearcher.get(m).singularIntSearch(74, 0,
                            Opcodes.BIPUSH).isFound(), clazz) != null)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* name ( Ljava/lang/String; ) */
        FieldNode name = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/lang/String;"));
        if(name != null)
            insert("name", clazz.name, name.name, name.desc);

        /* actions ( [Ljava/lang/String; ) */
        FieldNode actions = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[Ljava/lang/String;"));
        if(actions != null)
            insert("actions", clazz.name, actions.name, actions.desc);

        MethodNode readNextBuffer = classSearcher.findMethod(m ->
                !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") &&
                SearchUtils.containsParams(m, Utils.formatAsClass(Matchers.
                        getObfClass("Buffer"))) &&
                methodSearcher.get(m).singularIntSearch(74, 0,
                        Opcodes.BIPUSH).isFound());
        if(readNextBuffer != null) {
            insert("readNextBuffer", readNextBuffer);
            methodSearcher.setMethod(readNextBuffer);

            /* modelIDs ( [I ) */
            Pattern current = methodSearcher.searchGotoJump(Opcodes.GETFIELD,
                    "[I", 0, 0, clazz.name);
            if(current.isFound()) {
                insert("modelIDs", current.getFirstFieldNode());

                current = methodSearcher.jumpSearch(f -> {
                    FieldInsnNode fin = f.getFirstFieldNode();
                    return fin.owner.equals(clazz.name) && fin.desc.equals("[I") &&
                            !isHookFound(fin.name, true);
                }, Opcodes.GOTO, current.getFirstLine(), 200, 0,
                        Opcodes.GETFIELD);
            }

            /* modelTypes ( [I ) */
            if(current.isFound())
                insert("modelTypes", current.getFirstFieldNode());


            int instance = 0;
            int loops = 0;
            while((current = methodSearcher.linearSearch(instance,
                    Opcodes.BIPUSH, Opcodes.IF_ICMPNE)).isFound()) {
                if(loops > 100) break;
                IntInsnNode bipush = (IntInsnNode) current.getFirst();

                switch (bipush.operand) {

                    case 14:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* width ( I ) */
                        if(current.isFound())
                            insert("width", current.getFirstFieldNode());
                        break;

                    case 15:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* height ( I ) */
                        if(current.isFound())
                            insert("height", current.getFirstFieldNode());
                        break;

                    case 17:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "Z",
                                current.getFirstLine(), 0, clazz.name);
                        /* solid ( I ) */
                        if(current.isFound())
                            insert("solid", current.getFirstFieldNode());
                        break;

                    case 24:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* animationID ( I ) */
                        if(current.isFound())
                            insert("animationID", current.getFirstFieldNode());
                        break;

                    case 65:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* modelSizeX ( I ) */
                        if(current.isFound())
                            insert("modelSizeX", current.getFirstFieldNode());
                        break;

                    case 66:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* modelSizeY ( I ) */
                        if(current.isFound())
                            insert("modelSizeY", current.getFirstFieldNode());
                        break;

                    case 67:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* modelSizeZ ( I ) */
                        if(current.isFound())
                            insert("modelSizeZ", current.getFirstFieldNode());
                        break;

                    case 70:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* translateX ( I ) */
                        if(current.isFound())
                            insert("translateX", current.getFirstFieldNode());
                        break;

                    case 71:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* translateY ( I ) */
                        if(current.isFound())
                            insert("translateY", current.getFirstFieldNode());
                        break;

                    case 72:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                                current.getFirstLine(), 0, clazz.name);
                        /* translateZ ( I ) */
                        if(current.isFound())
                            insert("translateZ", current.getFirstFieldNode());
                        break;

                    case 74:
                        current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "Z",
                                current.getFirstLine(), 0, clazz.name);
                        /* unwalkable ( Z ) */
                        if(current.isFound())
                            insert("unwalkable", current.getFirstFieldNode());
                        break;

                    case 92:

                        current = methodSearcher.jumpSearch(null, Opcodes.GOTO,
                                current.getFirstLine(), 200, 0, Opcodes.PUTFIELD);
                        if(current.isFound()) {

                            FieldInsnNode fin = current.getFirstFieldNode();
                            if(fin.owner.equals(clazz.name)) {
                                if (fin.desc.equals("I")) {
                                    /* varpBitID ( I ) */
                                    insert("varpBitID", fin);
                                    current = methodSearcher.jumpSearch(null, Opcodes.GOTO,
                                            current.getFirstLine(), 200, 1, Opcodes.GETFIELD);

                                    /* configID ( I ) */
                                    if (current.isFound())
                                        insert("configID", current.getFirstFieldNode());

                                    /* childrenIDs ( [I ) */
                                } else if (fin.desc.equals("[I"))
                                    insert("childrenIDs", fin);
                            }
                        }

                        break;
                }

                instance++;
                loops++;
            }
        }

        //getModel, getAnimatedMode
        List<MethodNode> getModels = classSearcher.findMethods(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, Utils.formatAsClass(Matchers.getObfClass("Model"))) &&
                SearchUtils.containsParams(m, "[[I", "I"));

        MethodNode getModel = SearchUtils.getLeastInstructionLength(getModels.toArray(new MethodNode[0]));
        if(getModel != null) {
            insert("getModel", getModel);
            methodSearcher.setMethod(getModel);

            if(isHookFound("modelTypes")) {

                RSHook modelTypes = getHook("modelTypes");
                Pattern current = methodSearcher.searchForKnown(modelTypes.getObfOwner(),
                        modelTypes.getObfName());
                if(current.isFound()) {

                    current = methodSearcher.searchGotoJump(Opcodes.GETFIELD, "I",
                            current.getFirstLine(), 0, clazz.name);

                    /* id ( I ) */
                    if(current.isFound())
                        insert("id", current.getFirstFieldNode());

                }

            }
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "CacheNode" };
    }

}
