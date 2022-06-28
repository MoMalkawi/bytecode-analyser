package org.rsminion.classes.impl.io;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;
import org.rsminion.tools.utils.Utils;

public class BufferedFile extends RSClass {

    public BufferedFile() {
        super("BufferedFile", Matchers.Importance.LOW);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        /*
        TODO (Post Client Launch):
         accessFile, readBuffer, readBufferOffset, readBufferLength, writeBuffer,
         writeBufferOffset, writeBufferLength, offset, fileLength, length, fileOffset
         */
        return RSHook.EMPTY_ARRAY;
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) && !SearchUtils.isPublicFinal(clazz.access) &&
            SearchUtils.countObjectFields(clazz) == 11 && Searcher.classContainsFieldDesc(clazz,
                            Utils.formatAsClass(Matchers.getClass("FileOnDisk").getObfName())))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {}

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "FileOnDisk" };
    }

}
