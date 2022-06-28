package org.rsminion.classes.impl.asset;

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

//TODO: do the 4 [I fields if needed later.
public class AbstractPackage extends RSClass {

    public AbstractPackage() {
        super("AbstractPackage", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("checksum", "I", false), ////
                high("childIds", "[[I", false), ////
                //high("archiveIds", "[I", false),
                high("packedArchives", "[Ljava/lang/Object;", false),////
                high("unpackedArchives", "[[Ljava/lang/Object;", false),////
                high("removePacked", "Z", false), ////
                high("childIdentifiers", "[#LinearHashTable", false),////
                high("names", "#LinearHashTable", false),////
                //high("versions", "[I", false),
                high("sizes", "[I", false),////
                high("removeUnpacked", "Z", false), ////
                //high("checksums", "[I", false),
                high("size", "I", false),////
                //high("hashes", "[I", false),
                high("archiveFileNames", "[[I", false)////
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(Modifier.isAbstract(clazz.access) &&
            SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.countObjectFields(clazz) >= 10 &&
            Searcher.classContainsFieldDesc(clazz, Utils.formatAsClass(Matchers.
                    getClass("LinearHashTable").getObfName())))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode unpack = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "Z") && m.desc.contains("[I"));
        if(unpack != null) {
            insert("unpack", unpack);

            MethodSearcher methodSearcher = new MethodSearcher(unpack);

            /* sizes ( [I ) */
            Pattern sizes = methodSearcher.cycleInstances(
                    f -> methodSearcher.linearSearch(f, Opcodes.ALOAD, Opcodes.GETFIELD),
                    p -> {
                        FieldInsnNode fin = p.getFieldNodes().get(0);
                        return fin.owner.equals(clazz.name) && fin.desc.equals("[I");
                    }, 100
            );
            if(sizes != null)
                insert("sizes", sizes.getFieldNodes().get(0));

            /* childIds ( [[I ) */
            Pattern childIds = methodSearcher.cycleInstances(
                    f -> methodSearcher.linearSearch(f, Opcodes.ALOAD, Opcodes.GETFIELD),
                    p -> {
                        FieldInsnNode fin = p.getFieldNodes().get(0);
                        return fin.owner.equals(clazz.name) && fin.desc.equals("[[I");
                    }, 100
            );
            if(childIds != null) {
                insert("childIds", childIds.getFieldNodes().get(0));

                /* archiveFileNames ( [[I ) */
                FieldNode archiveFileNames = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("[[I") && !isHookFound(f.name, true));
                if(archiveFileNames != null)
                    insert("archiveFileNames", clazz.name, archiveFileNames.name, archiveFileNames.desc);
            }

            /* removePacked ( Z ) */
            Pattern removePacked = methodSearcher.cycleInstances(
                    i -> methodSearcher.linearSearch(i, Opcodes.GETFIELD, Pattern.IF_WILDCARD,
                            Opcodes.GOTO, Opcodes.ALOAD, Opcodes.GETFIELD),
                    p -> {
                        FieldInsnNode boolFin = (FieldInsnNode) p.getFirst();
                        FieldInsnNode arrFin = (FieldInsnNode) unpack.instructions.get(p.getFirstLine() + 5);
                        return (boolFin.owner.equals(clazz.name) && boolFin.desc.equals("Z")) &&
                                (arrFin.owner.equals(clazz.name) && arrFin.desc.equals("[Ljava/lang/Object;"));
                    }, 100
            );

            if(removePacked.isFound()) {
                insert("removePacked", removePacked.getFieldNodes().get(0));

                /* removeUnpacked ( Z ) */
                FieldNode removeUnpacked = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals("Z") && !isHookFound(f.name, true));
                if(removeUnpacked != null)
                    insert("removeUnpacked", clazz.name, removeUnpacked.name, removeUnpacked.desc);
            }
        }

        MethodNode parse = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") && m.desc.contains("[B"));

        if(parse != null) {
            insert("parse", parse);


        }

        /* packedArchives [Ljava/lang/Object; */
        FieldNode packedArchives = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[Ljava/lang/Object;"));
        if(packedArchives != null)
            insert("packedArchives", clazz.name, packedArchives.name, packedArchives.desc);

        /* unpackedArchives [[Ljava/lang/Object; */
        FieldNode unpackedArchives = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("[[Ljava/lang/Object;"));
        if(unpackedArchives != null)
            insert("unpackedArchives", clazz.name, unpackedArchives.name, unpackedArchives.desc);

        /* checksum ( I ) */
        FieldNode checksum = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                Modifier.isPublic(f.access) && f.desc.equals("I"));
        if(checksum != null)
            insert("checksum", clazz.name, checksum.name, checksum.desc);

        /* size ( I ) */
        FieldNode size = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                !Modifier.isPublic(f.access) && f.desc.equals("I") && !isHookFound(f.name, true));
        if(size != null)
            insert("size", clazz.name, size.name, size.desc);

        /* childIdentifiers ( [#LinearHashTable ) */
        FieldNode childIdentifiers = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(String.format("[%s", Utils.formatAsClass(Matchers.getClass("LinearHashTable").getObfName()))));
        if(childIdentifiers != null)
            insert("childIdentifiers", clazz.name, childIdentifiers.name, childIdentifiers.desc);

        /* names ( LinearHashTable ) */
        FieldNode names = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals(Utils.formatAsClass(Matchers.getClass("LinearHashTable").getObfName())));
        if(names != null)
            insert("names", clazz.name, names.name, names.desc);

    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] {
                "LinearHashTable"
        };
    }

}
