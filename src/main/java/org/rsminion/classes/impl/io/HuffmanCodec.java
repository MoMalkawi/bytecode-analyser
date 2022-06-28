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

@SuppressWarnings("unchecked")
public class HuffmanCodec extends RSClass {

    public HuffmanCodec() {
        super("HuffmanCodec", Matchers.Importance.LOW);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                low("masks", "[I", false),
                low("bits", "[B", false),
                low("keys", "[I", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.countObjectFields(clazz) == 3 &&
                    Utils.checkIntArrayMatch(Searcher.countFieldNodes(clazz,
                            ia -> !Modifier.isStatic(ia.access) && ia.desc.equals("[I"),
                            ba -> !Modifier.isStatic(ba.access) && ba.desc.equals("[B")), 2, 1))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {

        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode compress = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                m.desc.startsWith("([BII"));
        if(compress != null) {
            insert("compress", compress);

            /* masks ( [I ) */
            Pattern masks = new MethodSearcher(compress).singularSearch(f -> {
                FieldInsnNode fin = (FieldInsnNode) f;
                return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
            },0, Opcodes.GETFIELD);
            if(masks.isFound()) {
                insert("masks", masks.getFieldNodes().get(0));

                /* keys ( [I ) */
                FieldNode keys = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("[I") && !isHookFound(f.name, true));
                if(keys != null)
                    insert("keys", clazz.name, keys.name, keys.desc);

            }
        }

        /* bits ( [B ) */
        FieldNode bits = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[B"));
        if(bits != null)
            insert("bits", clazz.name, bits.name, bits.desc);
    }

    @Override
    protected String[] initRequiredClasses() {
        return Utils.EMPTY_ARRAY;
    }

}
