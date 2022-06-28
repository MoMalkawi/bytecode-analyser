package org.rsminion.classes;

import lombok.Getter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.core.matching.Matchers;
import org.rsminion.core.matching.data.RSHook;
import org.rsminion.core.matching.data.RSMethod;
import org.rsminion.tools.searchers.Searcher;
import org.rsminion.tools.utils.Filter;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class RSClass {

    protected ClassNode clazz;

    protected @Getter String obfName;

    protected @Getter String name;

    protected @Getter Matchers.Importance importance;

    private @Getter final Map<String, RSHook> hooks = new HashMap<>();

    //work on this and refactoring methods after completing the updater.
    private @Getter final Map<String, RSMethod> methods = new HashMap<>();

    private int matchFrequency = 0;

    public RSClass(String name, Matchers.Importance importance) {
        this.name = name;
        this.importance = importance;
        fillHooks(initRequiredHooks());
    }

    public RSClass find() {
        if(locateClass())
            locateHooks();
        return this;
    }

    protected abstract RSHook[] initRequiredHooks();

    protected abstract boolean locateClass();

    protected abstract void locateHooks();

    protected abstract String[] initRequiredClasses();

    protected void registerClass(ClassNode clazz) {
        if(clazz.name.startsWith("com/") || clazz.name.startsWith("org/"))
            return;
        if(matchFrequency > 0) {
            this.clazz = null;
            this.obfName = null;
        } else {
            this.clazz = clazz;
            this.obfName = clazz.name;
        }
        matchFrequency++;
    }

    protected MethodNode getConstructor() {
        return Searcher.findMethod(m -> !Modifier.isStatic(m.access) && m.name.equals("<init>"), clazz);
    }

    public boolean hasRequirements() {
        String[] requiredClasses = initRequiredClasses();
        RSClass clazz;
        for(String className : requiredClasses) {
            if((clazz = Matchers.getClass(className)) == null || !clazz.isFound())
                return false;
        }
        return true;
    }

    public void insert(String name, FieldInsnNode fin) {
        insert(name, fin.owner, fin.name, fin.desc);
    }

    public void insert(String name, String obfOwner, String obfName, String desc) {
        hooks.get(name).submitData(obfOwner,obfName,desc);
    }

    public void insert(String name, MethodNode method) {
        methods.put(name, RSMethod.create(name, this, method));
    }

    protected RSHook high(String name, String requiredDesc, boolean staticField) {
        return create(name, requiredDesc, staticField, Matchers.Importance.HIGH);
    }

    protected RSHook medium(String name, String requiredDesc, boolean staticField) {
        return create(name, requiredDesc, staticField, Matchers.Importance.MEDIUM);
    }

    protected RSHook low(String name, String requiredDesc, boolean staticField) {
        return create(name, requiredDesc, staticField, Matchers.Importance.LOW);
    }

    protected RSHook create(String name, String requiredDesc, boolean staticField, Matchers.Importance importance) {
        return new RSHook(getName(), name, requiredDesc, staticField, importance);
    }

    public RSHook getHook(String name) {
        return hooks.get(name);
    }

    public boolean isHookFound(String name) {
        return isHookFound(name, false);
    }

    public boolean containsHookType(String type) {
        return getHook(h -> h.getDesc().equals(type)) != null;
    }

    public boolean isHookFound(String name, boolean obf) {
        RSHook hook;
        if(!obf)
            return (hook = getHook(name)) != null && hook.isFound();

        return getHook(h -> h.getObfName().equals(name)) != null;
    }

    /**
     *
     * @param Filter Hook Filter
     * @return First Hook with Filter == true
     */
    public RSHook getHook(Filter<RSHook> Filter) {
        for(RSHook hook : hooks.values()) {
            if(hook.isFound() && Filter.verify(hook))
                return hook;
        }
        return null;
    }

    /**
     *
     * @param Filter Hook Filter
     * @return true if hook is found
     */
    public boolean isHookFound(Filter<RSHook> Filter) {
        return getHook(Filter) != null;
    }

    private void fillHooks(RSHook[] hooks) {
        for(RSHook hook : hooks)
            this.hooks.put(hook.getName(), hook);
    }

    public ClassNode getClassNode() {
        return clazz;
    }

    public boolean isFound() {
        return obfName != null;
    }

}
