package org.rsminion.classes.impl.collections;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
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
public class IterableQueue extends RSClass {

    private String nodeDesc;

    public IterableQueue() {
        super("IterableQueue", Matchers.Importance.HIGH); //TODO: check importance
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("current", "#Node", false),
                high("head", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        nodeDesc = Utils.formatAsClass(Matchers.getClass("Node").
                getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

            clazz.interfaces.size() >= 1 &&

            SearchUtils.hasInterfaces(clazz, "java/lang/Iterable") &&

            SearchUtils.countObjectFields(clazz) == 2 &&

            Searcher.countFieldNodes(clazz, n -> !Modifier.isStatic(n.access) &&
                    n.desc.equals(nodeDesc))[0] == 2)
                registerClass(clazz);
        }
        return isFound();
    }

    /**
     * Searches for isEmpty(), if it finds it (2)
     * If it doesn't find isEmpty(), it'll search for a non-static boolean method and (2)
     *
     * (2) Searches for INVOKEVIRTUAL, sets MethodSearcher to it if found.
     * Searches the method set in the MethodSearcher for GETFIELD and inserts it.
     * If GETFIELD wasn't found, it'll set the MethodSearcher to the parent method (isEmpty),
     * if it is in it's invoked method, and searches yet again.
     */
    @Override //Back-up patterns & pathways added.
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode isEmpty = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "Z") && m.name.equals("isEmpty"));
        MethodSearcher methodSearcher = new MethodSearcher();

        /* Locate isEmpty() & Locate Inner Methods */
        if(isEmpty == null || !findHead(isEmpty, classSearcher, methodSearcher)) {

            List<MethodNode> methods = classSearcher.findMethods(m -> !Modifier.isStatic(m.access) &&
                    SearchUtils.isReturnType(m, "Z") && SearchUtils.getParameters(m).length == 0);
            for(MethodNode method : methods) {
                isEmpty = method;
                if(findHead(isEmpty, classSearcher, methodSearcher))
                    break;
            }

        }

        if(isHookFound("head")) {

            FieldNode current = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                    f.desc.equals(nodeDesc) && !isHookFound(f.name, true));
            if(current != null)
                insert("current", clazz.name, current.name, current.desc);

        }

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

    private boolean findHead(MethodNode isEmpty, ClassSearcher classSearcher,  MethodSearcher methodSearcher) {
        methodSearcher.setMethod(isEmpty);

        /* If there's an InvokeVirtual, Set the Searcher to it. */
        Pattern result = methodSearcher.singularSearch(0, Opcodes.INVOKEVIRTUAL);
        if (result.isFound()) {
            MethodInsnNode min = (MethodInsnNode) result.getFirst();
            MethodNode method = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    m.name.equals(min.name) && m.desc.equals(min.desc));
            methodSearcher.setMethod(method);
        }

        /* head ( Node ) */ //Search for GETFIELD
        List<AbstractInsnNode> getFields = methodSearcher.getInstructions(a -> a.getOpcode() == Opcodes.GETFIELD);
        if((getFields.size() <= 0 || !insertHead(getFields)) &&
                !methodSearcher.getMethod().name.equals("isEmpty")) { //If not found and method isn't isEmpty
            methodSearcher.setMethod(isEmpty);
            getFields = methodSearcher.getInstructions(a -> a.getOpcode() == Opcodes.GETFIELD);
            return getFields.size() > 0  && insertHead(getFields);
        }
        return false;
    }

    private boolean insertHead(List<AbstractInsnNode> ains) {
        FieldInsnNode fin;
        for(AbstractInsnNode ain : ains) {
            fin = (FieldInsnNode) ain;
            if (fin.owner.equals(clazz.name) && fin.desc.equals(nodeDesc)) {
                insert("head", fin);
                return true;
            }
        }
        return false;
    }

}
