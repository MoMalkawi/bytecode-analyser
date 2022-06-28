package org.rsminion.classes.impl.config.name;

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
import java.util.List;

@SuppressWarnings("unchecked")
public class NameProvider extends RSClass {

    public NameProvider() {
        super("NameProvider", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("name", "#NameComposite", false),
                high("previousName", "#NameComposite", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.hasInterfaces(clazz, "java/lang/Comparable") &&
            clazz.interfaces.size() == 1 && Searcher.countFieldNodes(clazz,
                    n -> !Modifier.isStatic(n.access) && n.desc.equals(Utils.formatAsClass(
                            Matchers.getObfClass("NameComposite"))))[0] == 2)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        MethodNode method = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                !Modifier.isFinal(m.access) && SearchUtils.isReturnType(m, Utils.formatAsClass(
                        Matchers.getObfClass("NameComposite"))));

        if(method != null) {
            methodSearcher.setMethod(method);

            //Note: Same as the old pattern as my old updater
            /* name ( #NameComposite) */
            Pattern name = methodSearcher.linearSearch(p -> {
                FieldInsnNode fin = p.getFirstFieldNode();
                return fin.owner.equals(clazz.name) && fin.desc.equals(Utils.formatAsClass(
                        Matchers.getObfClass("NameComposite")));
            }, 0, method.instructions.size(), 0, Opcodes.ALOAD, Opcodes.GETFIELD);
            if(name.isFound())
                insert("name", name.getFirstFieldNode());

        }

        /* previousName ( #NameComposite ) */
        if(isHookFound("name")) {
            FieldNode previousName = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals(Utils.formatAsClass(
                            Matchers.getObfClass("NameComposite"))) && !isHookFound(f.name, true));
            if(previousName != null)
                insert("previousName", clazz.name, previousName.name, previousName.desc);
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "NameComposite" };
    }
}
