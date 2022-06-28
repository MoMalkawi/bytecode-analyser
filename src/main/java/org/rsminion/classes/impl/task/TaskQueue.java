package org.rsminion.classes.impl.task;

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

public class TaskQueue extends RSClass {

    public TaskQueue() {
        super("TaskQueue", Matchers.Importance.HIGH);
    }

    @Override
    protected RSHook[] initRequiredHooks() {
        return new RSHook[] {
                high("closed", "Z", false),
                high("currentTask", "#Task", false),
                high("cachedTask", "#Task", false),
                high("thread", "Ljava/lang/Thread;", false)
        };
    }

    @Override
    protected boolean locateClass() {
        for(ClassNode clazz : GamePack.getClasses().values()) {
            if(SearchUtils.isStandaloneObject(clazz) &&
            SearchUtils.hasInterfaces(clazz, "java/lang/Runnable") &&
            SearchUtils.countObjectFields(clazz) == 4 &&
                    Searcher.classContainsFieldDesc(clazz, Utils.formatAsClass(Matchers.
                            getClass("Task").getObfName())))
                registerClass(clazz);
        }
        return isFound();
    }

    @Override
    protected void locateHooks() {
        ClassSearcher classSearcher = new ClassSearcher(clazz);

        MethodNode run = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                SearchUtils.isReturnType(m, "V") && m.name.equals("run"));
        if(run == null) //Backup
            run = classSearcher.findMethod(m -> !Modifier.isStatic(m.access) &&
                    SearchUtils.isReturnType(m, "V") && !m.name.equals("<init>") &&
                    m.instructions.size() >= 100);

        if(run != null) {
            insert("run", run);

            MethodSearcher methodSearcher = new MethodSearcher(run);

            String fieldDesc = Utils.
                    formatAsClass(Matchers.getClass("Task").getObfName());

            /* currentTask ( Task ) */
            // ( if(currentTask == null) )
            Pattern currentTask = searchForCurrentTask(methodSearcher, Opcodes.GETFIELD, Opcodes.IFNONNULL);

            //Backup #1 ( if(currentTask != null) )
            if(!currentTask.isFound())
                currentTask = searchForCurrentTask(methodSearcher, Opcodes.GETFIELD, Opcodes.IFNULL);

            //Backup #2 ( Get first GetField )
            if(!currentTask.isFound())
                currentTask = methodSearcher.singularSearch(f -> {
                    FieldInsnNode fin = (FieldInsnNode) f;
                    return fin.owner.equals(clazz.name) && fin.desc.equals(fieldDesc);
                }, 0, Opcodes.GETFIELD);

            if(currentTask.isFound()) {
                insert("currentTask", currentTask.getFieldNodes().get(0));

                /* cachedTask ( Task ) */
                FieldNode cachedTask = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                        f.desc.equals(fieldDesc) && !isHookFound(f.name, true));
                if(cachedTask != null)
                    insert("cachedTask", clazz.name, cachedTask.name, cachedTask.desc);
            }
        }

        /* closed ( Z ) */
        FieldNode closed = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Z"));
        if(closed != null)
            insert("closed", clazz.name, closed.name, closed.desc);

        /* thread ( java/lang/Thread ) */
        FieldNode thread = classSearcher.findField(f -> !Modifier.isStatic(f.access) &&
                f.desc.equals("Ljava/lang/Thread;"));
        if(thread != null)
            insert("thread", clazz.name, thread.name, thread.desc);

    }

    private Pattern searchForCurrentTask(MethodSearcher methodSearcher, int... pattern) {
        return methodSearcher.cycleInstances(
                f -> methodSearcher.linearSearch(f, pattern),

                p -> {
                    FieldInsnNode fin = (FieldInsnNode) p.getFirst();
                    return fin.owner.equals(clazz.name) && fin.desc.equals(Utils.
                            formatAsClass(Matchers.getClass("Task").getObfName()));
                }, 100);
    }

    @Override
    protected String[] initRequiredClasses() {
        return new String[] {
                "Task"
        };
    }

}
