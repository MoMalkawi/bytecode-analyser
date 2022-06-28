package org.rsminion.classes.impl.collections;

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
public class Node extends RSClass {

    public Node() {
        super("Node", Matchers.Importance.HIGH);
    }

    public Node(String nodeName) {
        super(nodeName, Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("id", "J", false),
                high("next", "#Node", false),
                high("previous", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) && clazz.fields.size() >= 3) {
                if(Utils.checkIntArrayMatch(
                        Searcher.countFieldNodes(clazz,
                                j -> !Modifier.isStatic(j.access) && j.desc.equals("J"),
                                n -> !Modifier.isStatic(n.access) && n.desc.equals(Utils.formatAsClass(clazz.name))),
                        1, 2)) {
                    registerClass(clazz);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(getClassNode());

        String formattedClassDesc = Utils.formatAsClass(getObfName());

        /* Previous Link */
        MethodNode unlink = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isVoid(m) && !SearchUtils.isConstructor(m));
        if(unlink != null) {
            insert("unlink", unlink); //add method void unlink()
            Pattern pattern = new MethodSearcher(unlink).linearSearch(0,
                    Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFNONNULL);
            if(pattern.isFound()) insert("previous", (FieldInsnNode) pattern.get(1));
        }

        /* FieldNodes */
        FieldNode[] foundFields = classSearcher.findFields(
                i -> !Modifier.isStatic(i.access) && i.desc.equals("J"),
                p -> !Modifier.isStatic(p.access) && p.desc.equals(formattedClassDesc) &&
                        !isHookFound(p.name, true)
        );

        /* Node ID */
        if(foundFields[0] != null)
            insert("id", getObfName(), foundFields[0].name, foundFields[0].desc);

        /* Next Link */
        if(foundFields[1] != null)
            insert("next", getObfName(), foundFields[1].name, foundFields[1].desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }

}
