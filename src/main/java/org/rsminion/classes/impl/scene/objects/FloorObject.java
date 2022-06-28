package org.rsminion.classes.impl.scene.objects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
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
public class FloorObject extends RSClass {

    public FloorObject() {
        super("FloorObject", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "J", false),
                high("plane", "I", false),
                high("model", "#Renderable", false),
                high("x", "I", false),
                high("y", "I", false),
                high("flags", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.isStandaloneObject(clazz) &&
                    Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                            i -> !Modifier.isStatic(i.access) && i.desc.equals("I"),
                            r -> !Modifier.isStatic(r.access) && r.desc.equals(Utils.formatAsClass(Matchers.
                                    getObfClass("Renderable")))), 4, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        String renderable = Utils.formatAsClass(Matchers.getObfClass("Renderable"));

        /* id ( J ) */
        FieldNode id = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("J"));
        if(id != null)
            insert("id", clazz.name, id.name, id.desc);

        /* model ( #Renderable ) */
        FieldNode model = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(renderable));
        if(model != null)
            insert("model", clazz.name, model.name, model.desc);

        MethodNode addFloor = Searcher.deepFindMethod(m -> !Modifier.isStatic(m.access) &&
                m.desc.contains("IIII" + renderable + "J") && SearchUtils.countParam(m, renderable) == 1);

        if(addFloor != null) {
            methodSearcher.setMethod(addFloor);
            if(isHookFound("model")) {

                assert model != null;
                Pattern current = methodSearcher.searchForKnown(clazz.name, model.name);

                if(current.isFound())
                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                            1, clazz.name);

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

                /* plane ( I ) */
                if(current.isFound()) {
                    insert("plane", current.getFirstFieldNode());

                    current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD, "I", current.getFirstLine(),
                            1, clazz.name);
                }

                /* flags ( I ) */
                if(current.isFound())
                    insert("flags", current.getFirstFieldNode());

            }
        }

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }

}
