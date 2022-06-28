package org.rsminion.classes.impl.io;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;

import java.lang.reflect.Modifier;

public class DirectByteBuffer extends RSClass {

    public DirectByteBuffer() {
        super("DirectByteBuffer", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("buffer", "Ljava/nio/ByteBuffer;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(/*SearchUtils.isParent(clazz, "AbstractByteBuffer") &&*/
            !SearchUtils.isPublicFinal(clazz.access) && SearchUtils.countObjectFields(clazz) == 1
            && Searcher.classContainsFieldDesc(clazz, "Ljava/nio/ByteBuffer;")) {
                registerClass(clazz);
            }
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        /* buffer ( Ljava/nio/ByteBuffer; ) */
        FieldNode buffer = new ClassSearcher(clazz).findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/nio/ByteBuffer;"));
        if(buffer != null)
            insert("buffer", clazz.name, buffer.name, buffer.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "AbstractByteBuffer" };
    }

}
