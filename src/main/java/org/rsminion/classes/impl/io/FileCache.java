package org.rsminion.classes.impl.io;

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

public class FileCache extends RSClass {

    public FileCache() {
        super("FileCache", Matchers.Importance.LOW);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("volume", "#BufferedFile", false),
                low("index", "#BufferedFile", false),
                low("maximumSize", "I", false),
                low("id", "I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.countObjectFields(clazz) == 4 &&
                    Searcher.classContainsFieldDesc(clazz, Utils.formatAsClass(Matchers.
                            getClass("BufferedFile").getObfName())))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode method = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "Z") && SearchUtils.getParameters(m).length < 4
        && m.instructions.size() < 250);
        if(method != null) {

            MethodSearcher methodSearcher = new MethodSearcher(method);

            /* volume ( #BufferedFile ) */
            Pattern volume = methodSearcher.singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals(Utils.formatAsClass(Matchers.
                        getClass("BufferedFile").getObfName()));
            }, 0, Opcodes.GETFIELD);

            if(volume.isFound()) {
                insert("volume", (FieldInsnNode) volume.getFirst());

                /* index ( #BufferedFile ) */
                FieldNode index = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals(Utils.formatAsClass(Matchers.
                                getClass("BufferedFile").getObfName()))
                        && !isHookFound(f.name, true));
                if(index != null)
                    insert("index", clazz.name, index.name, index.desc);
            }

            /* maximumSize ( I ) */
            Pattern maximumSize = methodSearcher.singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("I");
            }, 0, Opcodes.GETFIELD);

            if(maximumSize.isFound()) {
                insert("maximumSize", (FieldInsnNode) maximumSize.getFirst());

                /* id ( I ) */
                FieldNode id = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("I") && !isHookFound(f.name, true));
                if(id != null)
                    insert("id", clazz.name, id.name, id.desc);
            }
        }
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "BufferedFile" };
    }

}
