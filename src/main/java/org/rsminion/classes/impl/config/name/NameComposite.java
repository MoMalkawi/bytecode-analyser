package org.rsminion.classes.impl.config.name;

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
public class NameComposite extends RSClass {

    public NameComposite() {
        super("NameComposite", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("name", "Ljava/lang/String;", false),
                high("formatted", "Ljava/lang/String;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
                    SearchUtils.hasInterfaces(clazz, "java/lang/Comparable") &&
                    Searcher.countFieldNodes(clazz, s -> !Modifier.isStatic(s.access) &&
                            s.desc.equals("Ljava/lang/String;"))[0] == 2)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);
        MethodSearcher methodSearcher = new MethodSearcher();

        MethodNode isFormattedNameValid = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "Z") && Utils.isBetween(SearchUtils.getParameters(m).length,
                -1, 2) && !m.desc.contains("Ljava/lang/Object;"));
        if(isFormattedNameValid != null) {
            insert("isFormattedNameValid", isFormattedNameValid);
            methodSearcher.setMethod(isFormattedNameValid);

            /* formatted ( Ljava/lang/String; ) */
            Pattern formatted = methodSearcher.singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("Ljava/lang/String;");
            }, 0, Opcodes.GETFIELD);
            if(formatted.isFound())
                insert("formatted", formatted.getFirstFieldNode());

        }

        /* name ( Ljava/lang/String; ) */
        if(isHookFound("formatted")) {
            FieldNode name = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    !isHookFound(f.name, true) &&
                    f.desc.equals("Ljava/lang/String;"));
            if(name != null)
                insert("name", clazz.name, name.name, name.desc);
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }
}
