package org.rsminion.classes.impl.io;

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
public class FileOnDisk extends RSClass {

    public FileOnDisk() {
        super("FileOnDisk", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("position", "J", false),
                low("file", "Ljava/io/RandomAccessFile;", false),
                low("length", "J", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.countObjectFields(clazz) == 3 &&
            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                    l -> !Modifier.isStatic(l.access) && l.desc.equals("J"),
                    raf -> !Modifier.isStatic(raf.access) &&
                            raf.desc.equals("Ljava/io/RandomAccessFile;")), 2, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode read = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isPublicFinal(m.access) && !SearchUtils.isReturnType(m, "V") &&
                m.desc.contains("[B") && SearchUtils.getParameters(m).length >= 2);
        if(read != null) {
            insert("read", read);

            /* position ( J ) */
            Pattern position = new MethodSearcher(read).
                    singularSearch(f -> {
                        FieldInsnNode fin = (FieldInsnNode) f;
                        return fin.owner.equals(clazz.name) && fin.desc.equals("J");
                    }, 0, Opcodes.GETFIELD);
            if(position.isFound()) {
                insert("position", (FieldInsnNode) position.getFirst());

                /* length ( J ) */
                FieldNode length = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                       f.desc.equals("J") && !isHookFound(f.name, true));
                if(length != null)
                    insert("length", clazz.name, length.name, length.desc);
            }
        }

        /* file ( Ljava/io/RandomAccessFile; ) */
        FieldNode file = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/io/RandomAccessFile;"));
        if(file != null)
            insert("file", clazz.name, file.name, file.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }

}
