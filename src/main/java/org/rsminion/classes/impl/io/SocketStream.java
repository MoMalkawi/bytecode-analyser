package org.rsminion.classes.impl.io;

import org.objectweb.asm.tree.ClassNode;
import org.rsminion.classes.RSClass;
import org.rsminion.core.gamepack.GamePack;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.SearchUtils;

@Deprecated
public class SocketStream extends RSClass {

    public SocketStream() {
        super("SocketStream", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("outBufferLen", "I", false),
                high("closed", "Z", false),
                high("streamOffset", "I", false),
                high("throwException", "Z", false),
                high("inputStream", "Ljava/io/InputStream;", false),
                high("outBuffer", "[B", false),
                high("socketThread", "#Task", false),
                high("outputStream", "Ljava/io/OutputStream;", false),
                high("manager", "#TaskQueue", false),
                high("socket", "Ljava/net/Socket;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.hasInterfaces(clazz, "java/lang/Runnable") &&
            SearchUtils.countObjectFields(clazz) >= 5 &&
            Searcher.classContainsFieldDesc(clazz, "Ljava/net/Socket;")) {
                System.out.println(clazz.name);
                registerClass(clazz);
            }
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Task", "TaskQueue" };
    }

}
