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

@SuppressWarnings("unchecked")
public class HashTable extends RSClass {

    protected String nodeDesc;

    public HashTable() {
        super("HashTable", Matchers.Importance.HIGH);
    }

    public HashTable(String hashTableName) {
        super(hashTableName, Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("size", "I", false),
                high("buckets", "[#Node", false),
                high("index", "I", false),
                high("head", "#Node", false),
                high("tail", "#Node", false)
        };
    }

    @Override
    protected boolean locateClass() {
        nodeDesc = Utils.formatAsClass(Matchers.getClass("Node").getObfName());
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&

            SearchUtils.isPublicFinal(clazz.access) &&

            SearchUtils.countObjectFields(clazz) == 5 &&

            !SearchUtils.hasInterfaces(clazz, "java/lang/Iterable") &&

            Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
            i -> !Modifier.isStatic(i.access) && i.desc.equals("I"),
            ht -> !Modifier.isStatic(ht.access) && ht.desc.equals(nodeDesc),
            b -> !Modifier.isStatic(b.access) && b.desc.equals("[" + nodeDesc)), 2, 2, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override //Added backup patterns
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode getTail = classSearcher.findMethod(m -> !Modifier.isStatic(m.access)
                && SearchUtils.isReturnType(m, nodeDesc)
                && SearchUtils.getParameters(m).length == 0
                && m.instructions.size() > 20 //If this breaks, use: this doesn't have invokevirtual
        );
        if(getTail != null) {
            insert("getTail", getTail);
            MethodSearcher methodSearcher = new MethodSearcher(getTail);
            /* Index & Size */
            FieldInsnNode size = null;
            FieldInsnNode index = null;
            /* Test the if_icmpge pattern ( size < index ) */
            Pattern greaterEquals = methodSearcher.cycleInstances(
                    t -> methodSearcher.singularSearch(t, Opcodes.IF_ICMPGE),
                    p -> checkForNativeFieldsUpwards(methodSearcher, p), 100);

            if(greaterEquals.isFound()) {
                //Index -> Size -> Branch
                AbstractInsnNode[] fieldNodes = greaterEquals.getFrom(10,
                        a -> a.getOpcode() == Opcodes.GETFIELD);
                if(fieldNodes.length >= 2) {
                    size = (FieldInsnNode) fieldNodes[fieldNodes.length - 1];
                    index = (FieldInsnNode) fieldNodes[fieldNodes.length - 2];
                }
            } else {

                /* Test the iflt pattern ( index >= size ) */
                Pattern lessThan = methodSearcher.cycleInstances(
                        t -> methodSearcher.singularSearch(t, Opcodes.IFLT),
                        p -> checkForNativeFieldsUpwards(methodSearcher, p), 100);

                if(lessThan.isFound()) {
                    //Index -> Size -> Branch
                    AbstractInsnNode[] fieldNodes = lessThan.getFrom(10,
                            a -> a.getOpcode() == Opcodes.GETFIELD);
                    if(fieldNodes.length >= 2) {
                        size = (FieldInsnNode) fieldNodes[fieldNodes.length - 1];
                        index = (FieldInsnNode) fieldNodes[fieldNodes.length - 2];
                    }
                }

            }

            if(size != null && index != null) {
                int difference = methodSearcher.compareInstructionFrequency(index, size);
                if(difference < 0) { //index has a higher frequency, compare and switch if otherwise.
                    FieldInsnNode temp = size;
                    size = index;
                    index = temp;
                }
                insert("size", size);
                insert("index", index);
            } else { //if either searches don't work, use the classic way of this.index > 0
                Pattern alternateIndex = methodSearcher.linearSearch(0,
                        Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IFLE);
                if(alternateIndex.isFound()) {
                    insert("index", (FieldInsnNode) alternateIndex.get(1));

                    /* Alternate size ( I ) */
                    FieldNode alternateSize = classSearcher.findField(f -> !Modifier.isStatic(f.access) && f.desc.equals("I") &&
                            !isHookFound(f.name, true));
                    if(alternateSize != null)
                        insert("size", clazz.name, alternateSize.name, alternateSize.desc);
                }
            }


            Pattern tail = methodSearcher.singularSearch(
                    a -> {
                        FieldInsnNode fin = (FieldInsnNode) a;
                        return fin.owner.equals(clazz.name) && fin.desc.equals(nodeDesc);
                    }, 0, Opcodes.GETFIELD);
            if(tail.isFound()) {
                insert("tail", (FieldInsnNode) tail.getFirst());

                FieldNode head = classSearcher.findField(f -> !Modifier.isStatic(f.access) && f.desc.equals(nodeDesc) &&
                        !isHookFound(f.name, true));
                if(head != null)
                    insert("head", clazz.name, head.name, head.desc);
            }

        }

        /* Buckets ( [Node ) */
        FieldNode buckets = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[" + nodeDesc));
        if(buckets != null)
            insert("buckets", clazz.name, buckets.name, buckets.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] { "Node" };
    }

    //Moved to method to avoid duplicate code
    private boolean checkForNativeFieldsUpwards(MethodSearcher methodSearcher, Pattern p) {
        int lineNumber = p.getFirstLine();
        AbstractInsnNode ain;
        FieldInsnNode fin;
        int nativeFields = 0;
        for(int i = (lineNumber - 1); i > (lineNumber - 10); i--) {
            if(nativeFields >= 2 || i < 0) break;
            if((ain = methodSearcher.getInstructions()[i]).getOpcode() == Opcodes.GETFIELD) {
                fin = (FieldInsnNode) ain;
                if(fin.owner.equals(clazz.name)) nativeFields++;
            } else if(ain instanceof JumpInsnNode)
                return false;
        }
        return nativeFields >= 2;
    }

}
