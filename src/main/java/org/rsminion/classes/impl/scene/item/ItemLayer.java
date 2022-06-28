package org.rsminion.classes.impl.scene.item;

import jdk.internal.org.objectweb.asm.Opcodes;
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
public class ItemLayer extends RSClass {

    private String renderableDesc;
    
    public ItemLayer() {
        super("ItemLayer", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("top", "#Renderable", false),
                high("middle", "#Renderable", false),
                high("bottom", "#Renderable", false),
                high("x", "I", false),
                high("y", "I", false),
                high("id", "J", false),//a.k.a tag
                high("flags", "I", false),//a.k.a tileHeight
                high("plane", "I", false),
        };
    }

    @Override
    protected boolean locateClass() {
        renderableDesc = Utils.formatAsClass(
                Matchers.getClass("Renderable").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isPublicFinal(clazz.access) &&
                    SearchUtils.isStandaloneObject(clazz) &&
                    Searcher.countFieldNodes(clazz,
                            r -> !Modifier.isStatic(r.access) && r.desc.equals(renderableDesc))[0] == 3)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        /* id ( I ) */
        FieldNode id = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("J"));
        if(id != null)
            insert("id", clazz.name, id.name, id.desc);

        /* addItemPile */
        MethodNode addItemPile = Searcher.deepFindMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") && SearchUtils.countParam(m, Utils.
                formatAsClass(Matchers.getObfClass("Renderable"))) == 3 &&
                SearchUtils.containsParams(m, "I"));

        if (addItemPile != null) {
            //TODO: add ability to add methods to other classes later on, not important
            methodSearcher.setMethod(addItemPile);

            //new ItemLayer()
            Pattern current = methodSearcher.linearSearch(0,
                    Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL);

            if(current.isFound())
                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD,
                        renderableDesc, current.getFirstLine(), 0, clazz.name);

            /* bottom ( #Renderable ) */
            if(current.isFound()) {
                insert("bottom", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD,
                        "I", current.getFirstLine(), 1, clazz.name);
            }

            /* x ( I ) */
            if(current.isFound()) {
                insert("x", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD,
                        "I", current.getFirstLine(), 1, clazz.name);
            }

            /* y ( I ) */
            if(current.isFound()) {
                insert("y", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD,
                        "I", current.getFirstLine(), 1, clazz.name);
            }

            /* flags ( I ) */
            if(current.isFound()) {
                insert("flags", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD,
                        renderableDesc, current.getFirstLine(), 1, clazz.name);
            }

            /* middle ( #Renderable ) */
            if(current.isFound()) {
                insert("middle", current.getFirstFieldNode());

                current = methodSearcher.searchGotoJump(Opcodes.PUTFIELD,
                        renderableDesc, current.getFirstLine(), 1, clazz.name);
            }

            /* top ( #Renderable ) */
            if(current.isFound())
                insert("top", current.getFirstFieldNode());

            /* plane ( I ) */
            FieldNode plane = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals("I") && !isHookFound(f.name, true));
            if(plane != null)
                insert("plane", clazz.name, plane.name, plane.desc);

        }

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Renderable" };
    }
}
