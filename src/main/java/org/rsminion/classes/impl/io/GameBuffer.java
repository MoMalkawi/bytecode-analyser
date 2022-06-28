package org.rsminion.classes.impl.io;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.ClassSearcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class GameBuffer extends RSClass {

    public GameBuffer() {
        super("GameBuffer", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("bitPosition", "I", false),
                high("cipher", "#IsaacCipher", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isParent(clazz, "Buffer") &&
            SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.countObjectFields(clazz) == 2)
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        /* bitPosition ( I ) */
        FieldNode bitPosition = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("I"));
        if(bitPosition != null)
            insert("bitPosition", clazz.name, bitPosition.name, bitPosition.desc);

        /* cipher ( #IsaacCipher ) */
        FieldNode cipher = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("IsaacCipher").getObfName())));
        if(cipher != null)
            insert("cipher", clazz.name, cipher.name, cipher.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Buffer", "IsaacCipher" };
    }

}
