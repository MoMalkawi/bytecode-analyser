package org.rsminion.classes.impl.devices;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

import java.lang.reflect.Modifier;

public class Keyboard extends RSClass {

    public Keyboard() {
        super("Keyboard", Matchers.Importance.LOW);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return RSHook.EMPTY_ARRAY;
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(Modifier.isFinal(clazz.access) && !Modifier.isAbstract(clazz.access) &&
            clazz.interfaces.size() == 2 && SearchUtils.hasInterfaces(clazz,
                    "java/awt/event/KeyListener"))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override protected void locateHooks() {}

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }
}
