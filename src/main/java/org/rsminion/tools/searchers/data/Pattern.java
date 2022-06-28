package org.rsminion.tools.searchers.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.rsminion.tools.utils.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
public class Pattern {

    public static final int SKIP_WILDCARD = -22022;
    public static final int BRANCH_WILDCARD = -22023; //159 -> 166
    public static final int CONST_WILDCARD = -22024; //1 -> 17
    public static final int IF_WILDCARD = -22025; //153 -> 158
    public static final int MUL_WILDCARD = -22026; //104 -> 107
    public static final int GET_WILDCARD = -22027; //178, 180
    public static final int PUT_WILDCARD = -22028; //179, 181
    public static final int LOAD_WILDCARD = -22029; //21 -> 25

    public static final Pattern EMPTY_PATTERN = new Pattern();

    private int[] opcodes;

    private int[] opcodesIndices;

    private @Setter int firstLine = -1; //start of the pattern

    private MethodNode method;

    public Pattern(int... opcodes) {
        this.opcodes = opcodes;
        opcodesIndices = new int[opcodes.length];
        reset();
    }

    public AbstractInsnNode getFirst() {
        return method.instructions.get(firstLine);
    }

    public FieldInsnNode getFirstFieldNode() {
        List<FieldInsnNode> fins = getFieldNodes();
        return fins.size() > 0 ? fins.get(0) : null;
    }

    public AbstractInsnNode get(int index) {
        return method.instructions.get(opcodesIndices[index]);
    }

    public AbstractInsnNode getFirstNodeByOpcode(int opcode) {
        for(int i = 0; i < opcodes.length; i++) {
            if(opcodes[i] == opcode)
                return get(i);
        }
        return null;
    }

    public List<FieldInsnNode> getFieldNodes() {
        List<FieldInsnNode> result = new ArrayList<>();
        AbstractInsnNode[] all = getAll();
        for(AbstractInsnNode ain : all) {
            if(ain instanceof FieldInsnNode)
                result.add((FieldInsnNode) ain);
        }
        return result;
    }

    public AbstractInsnNode[] getAll() {
        AbstractInsnNode[] instructions = new AbstractInsnNode[opcodes.length];
        for(int i = 0; i < instructions.length; i++)
            instructions[i] = method.instructions.get(opcodesIndices[i]);
        return instructions;
    }

    public AbstractInsnNode get(Filter<AbstractInsnNode> filter) {
        AbstractInsnNode[] all = getAll();
        for(AbstractInsnNode ain : all) {
            if(filter.verify(ain))
                return ain;
        }
        return null;
    }

    public AbstractInsnNode[] getFrom(int linesUp, Filter<AbstractInsnNode> filter) {
        List<AbstractInsnNode> result = new ArrayList<>();
        int firstIndex = firstLine - linesUp;
        if(firstIndex < 0) firstIndex = 0;
        AbstractInsnNode[] instructions = method.instructions.toArray();
        for(int i = firstIndex; i <= firstLine; i++) {
            AbstractInsnNode ain;
            if(filter.verify((ain = instructions[i])))
                result.add(ain);
        }
        return result.toArray(new AbstractInsnNode[0]);
    }

    //Only for linear searches
    public void fillLinearIndices() {
        int count = 0;
        while(count < opcodes.length) {
            opcodesIndices[count] = firstLine + count;
            count++;
        }
    }

    public int getLastLine() {
        return firstLine + (opcodes.length - 1);
    }

    public boolean isFound() {
        /*for(int index : opcodesIndices) {
            if(index == -1) return false;
        }*/
        return firstLine != -1;
    }

    public boolean isEmpty() {
        return firstLine == -1;
    }

    public void reset() {
        Arrays.fill(opcodesIndices, -1);
    }

    public static Pattern createSingular(MethodNode method, int opcode, int index) {
        Pattern pattern = new Pattern(opcode);
        pattern.firstLine = index;
        pattern.opcodesIndices[0] = index;
        pattern.method = method;
        return pattern;
    }

    public static Pattern createLinear(MethodNode method, int firstIndex, int... opcodes) {
        Pattern pattern = new Pattern(opcodes);
        pattern.firstLine = firstIndex;
        pattern.fillLinearIndices();
        pattern.method = method;
        return pattern;
    }

}
