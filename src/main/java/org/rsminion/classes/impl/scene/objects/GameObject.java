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

@SuppressWarnings("unchecked")
public class GameObject extends RSClass {
    
    public GameObject() {
        super("GameObject", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "J", false),
                high("flags", "I", false),
                high("plane", "I", false),
                high("x", "I", false),
                high("y", "I", false),
                high("height", "I", false),
                high("renderable", "#Renderable", false),
                high("orientation", "I", false),
                high("relativeX", "I", false),
                high("relativeY", "I", false),
                high("offsetX", "I", false),
                high("offsetY", "I", false),
                //Do these later (After launch), found in draw(LTile;Z) in Region
                //high("drawPriority", "I", false),
                //high("cycle", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.isStandaloneObject(clazz) &&
            clazz.interfaces.size() <= 0 &&
            Utils.isBetween(SearchUtils.countObjectFields(clazz),12, 20) &&
            SearchUtils.countObjectMethods(clazz) <= 1 &&
            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    r -> !Modifier.isStatic(r.access) && r.desc.equals(Utils.
                            formatAsClass(Matchers.
                                    getObfClass("Renderable"))),
                    i -> !Modifier.isStatic(i.access) && i.desc.equals("I")),1,12))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* id ( J ) */
        FieldNode id = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("J"));
        if(id != null)
            insert("id", clazz.name, id.name, id.desc);

        /* renderable ( #Renderable ) */
        FieldNode renderable = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getObfClass("Renderable"))));
        if(renderable != null)
            insert("renderable", clazz.name, renderable.name, renderable.desc);

        String renderableF = Utils.formatAsClass(Matchers.getObfClass("Renderable"));
        MethodNode addEntityMarker = Searcher.deepFindMethod(f -> !Modifier.isStatic(f.access) &&
                f.desc.contains("IIIIIIII" + renderableF + "IZ") &&
                SearchUtils.countParam(f, renderableF) == 1);
        if(addEntityMarker != null) {
            methodSearcher.setMethod(addEntityMarker);

            if(isHookFound("id")) {

                assert id != null;
                Pattern current = methodSearcher.searchForKnown(clazz.name, id.name);
                //hg.c L20 -> L21
                //hg.j L21 -> L24
                //hg.v, hg.h L24 -> L25
                //hg.g L25 -> L28
                //hg.o, hg.l, hg.n L28 -> L29
                //hg.d L29 -> L22
                //hg.u L22 -> L23
                //hg.f L23 -> L26
                //hg.r L26 -> L27
                if(current.isFound())
                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);

                /* flags ( I ) */
                if(current.isFound()) {
                    insert("flags", current.getFirstFieldNode());

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
                            current.getFirstLine(), 1, clazz.name);
                }

                /* height ( I ) */
                if(current.isFound()) {
                    insert("height", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);
                }

                /* orientation ( I ) */
                if(current.isFound()) {
                    insert("orientation", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);
                }

                /* relativeX ( I ) */
                if(current.isFound()) {
                    insert("relativeX", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);
                }

                /* relativeY ( I ) */
                if(current.isFound()) {
                    insert("relativeY", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);
                }

                /* offsetX ( I ) */
                if(current.isFound()) {
                    insert("offsetX", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I",
                            current.getFirstLine(), 1, clazz.name);
                }

                /* offsetY ( I ) */
                if(current.isFound())
                    insert("offsetY", current.getFirstFieldNode());
            }

        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }
}
